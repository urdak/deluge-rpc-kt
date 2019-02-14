package net.ickis.deluge

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.RawRequest
import net.ickis.deluge.request.Request
import java.util.HashMap

internal sealed class DelugeEvent {
    /**
     * An outgoing request event, containing the [request] data, as well as the [deferred] that is responsible for
     * producing a result for the [request].
     */
    internal data class Outgoing<T>(
            val request: Request<T>,
            val deferred: CompletableDeferred<T>
    ) : DelugeEvent() {
        fun serialize(id: Int) = RawRequest(request.serialize(id))
    }

    /**
     * An incoming response event, containing the parsed [response] that is received from the daemon.
     */
    internal data class Incoming(val response: DelugeResponse) : DelugeEvent() {
        fun process(deferred: CompletableDeferred<*>) {
            when (response) {
                is DelugeResponse.Value -> {
                    @Suppress("UNCHECKED_CAST")
                    deferred as CompletableDeferred<Any>
                    deferred.complete(response.value)
                }
                is DelugeResponse.Error -> deferred.completeExceptionally(response.exception)
                is DelugeResponse.Event -> TODO("HANDLE EVENTS ${response.value}")
            }
        }
    }
}

internal fun CoroutineScope.eventHandler(socket: DelugeSocket) = actor<DelugeEvent> {
    val requests = HashMap<Int, CompletableDeferred<*>>()
    var counter = 0
    for (event in channel) {
        when (event) {
            is DelugeEvent.Outgoing<*> -> {
                val id = counter++
                requests[id] = event.deferred
                socket.write(event.serialize(id))
            }
            is DelugeEvent.Incoming -> {
                val request = requests.remove(event.response.requestId)
                if (request != null) {
                    event.process(request)
                } else {
                    TODO("LOG ME")
                }
            }
        }
    }
}
