package net.ickis.deluge.samples

import net.ickis.deluge.api.DelugeClient
import net.ickis.deluge.request.SimpleRequest

class CustomCommand(param: Int) : SimpleRequest<Int>("custom_command") {
    override val args: List<Any> = listOf(param)
}

suspend fun DelugeClient.customCommand(param: Int) = request(CustomCommand(param))

fun main() {
    simpleClient { client ->
        val result: Int = client.customCommand(5)
    }
}
