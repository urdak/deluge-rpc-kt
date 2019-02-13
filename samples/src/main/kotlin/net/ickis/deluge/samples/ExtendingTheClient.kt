package net.ickis.deluge.samples

import net.ickis.deluge.api.DelugeClient
import net.ickis.deluge.request.Request

object FreeSpace : Request<Int>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}

suspend fun DelugeClient.freeSpace() = request(FreeSpace)
