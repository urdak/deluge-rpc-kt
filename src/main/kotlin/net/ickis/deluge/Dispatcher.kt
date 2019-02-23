package net.ickis.deluge

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.RawRequest
import net.ickis.deluge.request.Request
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.HashMap

private val logger: Logger = LogManager.getLogger()

internal sealed class DispatcherEvent {
    /**
     * An outgoing request event, containing the [request] data, as well as the [deferred] that is responsible for
     * producing a result for the [request].
     */
    internal data class Outgoing<T>(
            val request: Request<T>,
            val deferred: CompletableDeferred<T>
    ) : DispatcherEvent() {
        fun serialize(id: Int) = RawRequest(request.serialize(id))
    }

    /**
     * An incoming response event, containing the parsed [response] that is received from the daemon.
     */
    internal data class Incoming(val response: DelugeResponse) : DispatcherEvent()
}

internal fun CoroutineScope.dispatcher(socket: DelugeSocket) = actor<DispatcherEvent> {
    val activeEvents = HashMap<Int, DispatcherEvent.Outgoing<*>>()
    var counter = 0
    for (event in channel) {
        logger.info("Processing $event")
        when (event) {
            is DispatcherEvent.Outgoing<*> -> {
                val id = counter++
                activeEvents[id] = event
                socket.write(event.serialize(id))
            }
            is DispatcherEvent.Incoming -> {
                when (val response = event.response) {
                    is DelugeResponse.Value -> {
                        val activeEvent = activeEvents.remove(response.requestId)
                        if (activeEvent != null) {
                            try {
                                val responseValue = activeEvent.request.createResponse(response.value)
                                @Suppress("UNCHECKED_CAST")
                                val deferred = activeEvent.deferred as CompletableDeferred<Any?>
                                deferred.complete(responseValue)
                            } catch (ex: Exception) {
                                activeEvent.deferred.completeExceptionally(ex)
                            }
                        } else {
                            logger.warn("Received $response not found in local cache")
                        }
                    }
                    is DelugeResponse.Error -> {
                        val activeEvent = activeEvents.remove(response.requestId)
                        if (activeEvent != null) {
                            activeEvent.deferred.completeExceptionally(response.exception)
                        } else {
                            logger.warn("Received $response not found in local cache")
                        }
                    }
                    is DelugeResponse.Event -> TODO("Handle events")
                }
            }
        }
    }
}
