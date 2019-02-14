package net.ickis.deluge.request

/**
 * Retrieves the information about a torrent file specified by the [torrentId].
 */
class TorrentStatus(
        private val torrentId: String
) : Request<Map<String, Map<String, Any>>>("core.get_torrent_status") {
    override val args: List<Any>
        get() = listOf(torrentId, listOf<String>())
}
