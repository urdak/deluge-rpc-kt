package net.ickis.deluge

import kotlinx.coroutines.CompletableDeferred
import net.ickis.deluge.net.SerializedRequest
import net.ickis.deluge.request.Request

internal sealed class DelugeEvent {
    internal data class Outgoing<T>(val request: Request<T>,
                                    val deferred: CompletableDeferred<T>) : DelugeEvent() {
        fun serialize(id: Int) = SerializedRequest(request.serialize(id))
    }

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
