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
import java.util.*
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
    fun process(subscribedMap: MutableMap<String, List<Subscribe<*>>>) {
        logger.debug {
            "Subscribing to event ${request.event.name} (${request.event::class.java.canonicalName})"
        }
        subscribedMap.compute(request.event.name) { _, v ->
            if (v == null) listOf(this) else v + this
        }
    }
}

/**
 * An incoming response command, containing the parsed [response] that is received from the daemon.
 */
internal data class Receive(val response: DelugeResponse) : DispatcherCommand() {
    /**
     * Processes the response depending on its type.
     */
    suspend fun process(
            sentMap: MutableMap<Int, Send<*>>,
            subscribedMap: MutableMap<String, List<Subscribe<*>>>
    ) {
        when (response) {
            is DelugeResponse.Value -> response.processValue(sentMap)
            is DelugeResponse.Error -> response.processError(sentMap)
            is DelugeResponse.Event -> response.processEvent(subscribedMap)
        }.exhaustive
    }

    /**
     * Processes the response as a value. Attempts to create a responses type and complete the deferred.
     * If the response creation throws an exception, the deferred is completed exceptionally using that exception.
     * Ignores the response if the request is not found in the [sentMap].
     */
    private fun DelugeResponse.Value.processValue(sentMap: MutableMap<Int, Send<*>>) {
        val sendCommand = sentMap.remove(requestId)
        if (sendCommand != null) {
            try {
                val responseValue = sendCommand.request.createResponse(value)
                @Suppress("UNCHECKED_CAST")
                val deferred = sendCommand.deferred as CompletableDeferred<Any?>
                deferred.complete(responseValue)
            } catch (ex: Exception) {
                logger.error(ex) { "Failed to create response from $value" }
                sendCommand.deferred.completeExceptionally(ex)
            }
        } else {
            logger.warn { "Received $response not found in local cache" }
        }
    }

    /**
     * Processes the response as an error. Completes the deferred exceptionally using the exception from the error.
     * Ignores the response if the request is not found in the [sentMap].
     */
    private fun DelugeResponse.Error.processError(sentMap: MutableMap<Int, Send<*>>) {
        val sendCommand = sentMap.remove(requestId)
        if (sendCommand != null) {
            sendCommand.deferred.completeExceptionally(exception)
        } else {
            logger.warn { "Received $response not found in local cache" }
        }
    }

    /**
     * Processes the response as an event. Attempts to create notifications and send them to subscribed channels.
     * If the notification creation throws an exception, the channel is closed using that exception and removed from the
     * subscriptions.
     * If the channel is closed for receive (the user closed the channel), the channel is also removed from the
     * subscriptions.
     * Ignores the response if the subscription is not found in the [subscribedMap].
     */
    private suspend fun DelugeResponse.Event.processEvent(subscribedMap: MutableMap<String, List<Subscribe<*>>>) {
        val subscriptions = subscribedMap[eventName]
        if (subscriptions.isNullOrEmpty()) {
            logger.debug { "No subscriptions for $eventName" }
            return
        }
        subscribedMap[eventName] = subscriptions.filter {
            if (it.channel.isClosedForReceive) {
                logger.debug { "Remove ${it.request.event::class.java.canonicalName} - channel closed by client" }
                false
            } else {
                try {
                    val notification: Any? = it.request.event.createNotification(value)
                    @Suppress("UNCHECKED_CAST")
                    val channel = it.channel as Channel<Any?>
                    channel.send(notification)
                    true
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to create notification from $value" }
                    it.channel.close(ex)
                    false
                }
            }
        }
    }
}

/**
 * Creates a dispatcher that keeps track of ongoing requests and subscriptions.
 * @param socket The socket that is used to send data with the [Send] command.
 */
internal fun CoroutineScope.dispatcher(socket: DelugeSocket) = actor<DispatcherCommand> {
    val sentMap = HashMap<Int, Send<*>>()
    val subscribedMap = HashMap<String, List<Subscribe<*>>>()
    var counter = 0
    for (command in channel) {
        logger.info { "Processing $command" }
        when (command) {
            is Send<*> -> command.process(socket, counter++, sentMap)
            is Subscribe<*> -> command.process(subscribedMap)
            is Receive -> command.process(sentMap, subscribedMap)
        }.exhaustive
    }
}
