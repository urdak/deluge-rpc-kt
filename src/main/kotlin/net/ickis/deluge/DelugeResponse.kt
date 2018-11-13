package net.ickis.deluge

import java.io.IOException

internal sealed class DelugeResponse constructor(open val requestId: Int) {
    companion object {
        fun create(list: List<*>): DelugeResponse {
            if (list.size < 3) {
                throw IOException("Response does not contain sufficient data: $list")
            }
            val messageType = list[0] as? Int
                    ?: throw IOException("Expected an integer message type, but got ${list[0]}")
            val requestId = (list[1] as? Number)?.toInt()
                    ?: throw IOException("Expected a number request id, but got ${list[1]}")
            val value = list[2]
                    ?: return Error.fromMessage(requestId, "Response returned a null value")
            return when (messageType) {
                1 -> Value(requestId, value)
                2 -> Error.fromAny(requestId, value)
                3 -> Event(requestId, value)
                else -> throw IOException("Unknown message type $messageType")
            }
        }
    }

    internal data class Value(override val requestId: Int, val value: Any)
        : DelugeResponse(requestId)

    internal data class Error(
            override val requestId: Int,
            val exception: DelugeException
    ) : DelugeResponse(requestId) {
        companion object {
            fun fromMessage(requestId: Int, message: String) = Error(requestId, DelugeException(
                    message = message))

            fun fromAny(requestId: Int, value: Any) = if (value is List<*> && value.size >= 3) {
                Error(requestId, DelugeException(value[0] as? String, value[1] as? String, value[2] as? String))
            } else {
                fromMessage(requestId, value.toString())
            }
        }
    }

    internal data class Event(override val requestId: Int, val value: Any)
        : DelugeResponse(requestId)
}
