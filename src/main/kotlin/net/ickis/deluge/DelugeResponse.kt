package net.ickis.deluge

import net.ickis.deluge.net.RawResponse
import java.io.IOException

/**
 * RPC Response types.
 * @see [Deluge RPC](https://deluge.readthedocs.io/en/develop/reference/rpc.html)
 */
internal sealed class DelugeResponse {
    companion object {
        /**
         * Throws [IOException] if the size of the list is less than the required size.
         */
        fun List<*>.requireSize(requiredSize: Int) {
            if (size < requiredSize) {
                throw IOException("Response does not contain sufficient data: $this")
            }
        }

        /**
         * Creates a [DelugeResponse] from deserialized raw response data.
         */
        fun create(rawResponse: RawResponse): DelugeResponse {
            val list = rawResponse.data
            if (list.size < 3) {
                throw IOException("Response does not contain sufficient data: $list")
            }
            val messageType = list[0] as? Int
                    ?: throw IOException("Expected an integer message type, but got ${list[0]}")
            return when (messageType) {
                1 -> Value.create(list)
                2 -> Error.create(list)
                3 -> Event.create(list)
                else -> throw IOException("Unknown message type $messageType")
            }
        }
    }

    /**
     * Represents a value response from the daemon.
     */
    internal data class Value(val requestId: Int, val value: Any) : DelugeResponse() {
        companion object {
            fun create(list: List<*>): Value {
                list.requireSize(3)
                val requestId = (list[1] as? Number)?.toInt()
                        ?: throw IOException("Expected a number request id, but got ${list[1]}")
                val value = list[2]
                        ?: throw IOException("Response with id $requestId returned a null value")
                return Value(requestId, value)
            }
        }
    }

    /**
     * Represents an error response from the daemon.
     */
    internal data class Error(
            val requestId: Int,
            val exception: DelugeException
    ) : DelugeResponse() {
        companion object {
            fun create(list: List<*>): Error {
                val value = Value.create(list)
                return Error.fromAny(value.requestId, value.value)
            }

            private fun fromMessage(requestId: Int, message: String) = Error(requestId, DelugeException(
                    message = message))

            private fun fromAny(requestId: Int, value: Any) = if (value is List<*> && value.size >= 3) {
                Error(requestId, DelugeException(value[0] as? String, value[1] as? String, value[2] as? String))
            } else {
                fromMessage(requestId, value.toString())
            }
        }
    }

    /**
     * Represents an event response from the daemon.
     */
    internal data class Event(val eventName: String, val value: List<*>) : DelugeResponse() {
        companion object {
            fun create(list: List<*>): Event {
                list.requireSize(3)
                val eventName = list[1] as? String ?: throw IOException("Expected event_name string, but got ${list[1]}")
                val args = list[2] as? List<*> ?: throw IOException("Expected data list, but got ${list[2]}")
                return Event(eventName, args)
            }
        }
    }
}
