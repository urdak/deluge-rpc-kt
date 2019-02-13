package net.ickis.deluge.request

object FreeSpace : Request<Long>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}
