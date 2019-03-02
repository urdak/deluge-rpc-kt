package net.ickis.deluge

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import mu.KotlinLogging
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.RawRequest
import net.ickis.deluge.request.EventRequest
import net.ickis.deluge.request.Request
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

private val logger = KotlinLogging.logger {}

internal sealed class DispatcherCommand

/**
 * An outgoing request command, containing the [request] data, as well as the [deferred] that is responsible for
 * producing a result for the [request].
 */
internal data class Send<T>(
        val request: Request<T>,
        val deferred: CompletableDeferred<T>
) : DispatcherCommand() {
    suspend fun process(socket: DelugeSocket, id: Int, sentMap: MutableMap<Int, Send<*>>) {
        val rawRequest = RawRequest(request.serialize(id))
        logger.debug { "Send request $rawRequest with id $id" }
        sentMap[id] = this
        socket.write(rawRequest)
    }
}

/**
 * A subscription command, containing the [request] event, as well as the [channel] that is responsible for providing
 * notifications for the [request].
 */
internal data class Subscribe<T>(
        val request: EventRequest,
        val channel: Channel<T>
): DispatcherCommand() {
    fun process(subscribedMap: MutableMap<String, MutableList<Subscribe<*>>>) {
        logger.debug {
            "Subscribing to event ${request.event.name} (${request.event::class.java.canonicalName})"
        }
        subscribedMap.computeIfAbsent(request.event.name) { ArrayList() }.add(this)
    }
}

/**
 * An incoming response command, containing the parsed [response] that is received from the daemon.
 */
internal data class Receive(val response: DelugeResponse) : DispatcherCommand() {
    suspend fun process(
            sentMap: MutableMap<Int, Send<*>>,
            subscribedMap: MutableMap<String, MutableList<Subscribe<*>>>
    ) {
        when (response) {
            is DelugeResponse.Value -> response.processValue(sentMap)
            is DelugeResponse.Error -> response.processError(sentMap)
            is DelugeResponse.Event -> response.processEvent(subscribedMap)
        }.exhaustive
    }

    private fun DelugeResponse.Value.processValue(sentMap: MutableMap<Int, Send<*>>) {
        val sendCommand = sentMap.remove(requestId)
        if (sendCommand != null) {
            try {
                val responseValue = sendCommand.request.createResponse(value)
                @Suppress("UNCHECKED_CAST")
                val deferred = sendCommand.deferred as CompletableDeferred<Any?>
                deferred.complete(responseValue)
            } catch (ex: Exception) {
                sendCommand.deferred.completeExceptionally(ex)
            }
        } else {
            logger.warn { "Received $response not found in local cache" }
        }
    }

    private fun DelugeResponse.Error.processError(sentMap: MutableMap<Int, Send<*>>) {
        val sendCommand = sentMap.remove(requestId)
        if (sendCommand != null) {
            sendCommand.deferred.completeExceptionally(exception)
        } else {
            logger.warn { "Received $response not found in local cache" }
        }
    }

    private suspend fun DelugeResponse.Event.processEvent(subscribedMap: MutableMap<String, MutableList<Subscribe<*>>>) {
        val subscriptions = subscribedMap[eventName]
        if (subscriptions != null) {
            val iterator = subscriptions.iterator()
            while (iterator.hasNext()) {
                val subscription = iterator.next()
                if (subscription.channel.isClosedForReceive) {
                    logger.debug {
                        "Remove ${subscription.request.event::class.java.canonicalName} - channel closed by client"
                    }
                    iterator.remove()
                    continue
                }
                val notification: Any? = subscription.request.event.createNotification(value)
                @Suppress("UNCHECKED_CAST")
                val channel = subscription.channel as Channel<Any?>
                channel.send(notification)
            }
        } else {
            logger.debug { "No subscriptions for $eventName" }
        }
    }
}

internal fun CoroutineScope.dispatcher(socket: DelugeSocket) = actor<DispatcherCommand> {
    val sentMap = HashMap<Int, Send<*>>()
    val subscribedMap = HashMap<String, MutableList<Subscribe<*>>>()
    var counter = 0
    for (command in channel) {
        logger.info { "Processing $command" }
        when (command) {
            is Send<*> -> command.process(socket, counter++, sentMap)
            is Subscribe<*> -> command.process(subscribedMap)
            is Receive -> command.process(sentMap, subscribedMap)
        }
    }
}
