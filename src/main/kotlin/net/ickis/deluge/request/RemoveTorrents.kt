package net.ickis.deluge.request

/**
 * Remove the torrent using the specified torrent id. Uses the [removeData] parameter to determine whether the data
 * should also be removed.
 */
class RemoveTorrents(
        private val torrentId: String,
        private val removeData: Boolean
) : Request<Boolean?>("core.remove_torrent") {
    override val args: List<Any>
        get() = listOf(torrentId, removeData)
}
