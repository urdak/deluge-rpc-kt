package net.ickis.deluge.samples

import net.ickis.deluge.api.DelugeClient
import net.ickis.deluge.request.Request

class CustomCommand(param: Int) : Request<Int>("custom_command") {
    override val args: List<Any> = listOf(param)
}

suspend fun DelugeClient.customCommand(param: Int) = request(CustomCommand(param))
