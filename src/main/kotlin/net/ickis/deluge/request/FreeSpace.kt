package net.ickis.deluge.request

/**
 * Retrieves the number of free bytes available in the default download location.
 */
object FreeSpace : SimpleRequest<Long>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}
