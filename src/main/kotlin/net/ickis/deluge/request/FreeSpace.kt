package net.ickis.deluge.request

object FreeSpace : Request<Int>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}
