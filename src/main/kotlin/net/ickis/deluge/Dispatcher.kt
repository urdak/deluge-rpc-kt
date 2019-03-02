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

internal sealed class DispatcherCommand {
    /**
     * An outgoing request event, containing the [request] data, as well as the [deferred] that is responsible for
     * producing a result for the [request].
     */
    data class Send<T>(
            val request: Request<T>,
            val deferred: CompletableDeferred<T>
    ) : DispatcherCommand() {
        fun serialize(id: Int) = RawRequest(request.serialize(id))
    }

    data class Subscribe<T>(
            val request: EventRequest,
            val channel: Channel<T>
    ): DispatcherCommand()

    /**
     * An incoming response event, containing the parsed [response] that is received from the daemon.
     */
    data class Receive(val response: DelugeResponse) : DispatcherCommand()
}

internal fun CoroutineScope.dispatcher(socket: DelugeSocket) = actor<DispatcherCommand> {
    val sentMap = HashMap<Int, DispatcherCommand.Send<*>>()
    val subscribedMap = HashMap<String, MutableList<DispatcherCommand.Subscribe<*>>>()
    var counter = 0
    for (command in channel) {
        logger.info { "Processing $command" }
        when (command) {
            is DispatcherCommand.Send<*> -> {
                val id = counter++
                val rawRequest = command.serialize(id)
                logger.debug { "Send request $rawRequest with id $id" }
                sentMap[id] = command
                socket.write(rawRequest)
            }
            is DispatcherCommand.Subscribe<*> -> {
                logger.debug {
                    "Subscribing to event ${command.request.event.name} (${command.request.event::class.java.canonicalName})"
                }
                subscribedMap.computeIfAbsent(command.request.event.name) { ArrayList() }.add(command)
            }
            is DispatcherCommand.Receive -> {
                when (val response = command.response) {
                    is DelugeResponse.Value -> {
                        val sendCommand = sentMap.remove(response.requestId)
                        if (sendCommand != null) {
                            try {
                                val responseValue = sendCommand.request.createResponse(response.value)
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
                    is DelugeResponse.Error -> {
                        val sendCommand = sentMap.remove(response.requestId)
                        if (sendCommand != null) {
                            sendCommand.deferred.completeExceptionally(response.exception)
                        } else {
                            logger.warn { "Received $response not found in local cache" }
                        }
                    }
                    is DelugeResponse.Event -> {
                        val subscriptions = subscribedMap[response.eventName]
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
                                val notification: Any? = subscription.request.event.createNotification(response.value)
                                @Suppress("UNCHECKED_CAST")
                                val channel = subscription.channel as Channel<Any?>
                                channel.send(notification)
                            }
                        }
                    }
                }
            }
        }
    }
}
