package net.ickis.deluge.request

import net.ickis.deluge.api.Torrent

/**
 * Retrieves the information about a torrent file specified by the [torrentId].
 */
class TorrentStatus(
        private val torrentId: String
) : Request<Torrent>("core.get_torrent_status") {
    @Suppress("UNCHECKED_CAST")
    override fun createResponse(rawValue: Any?) = Torrent(rawValue as Map<String, Map<String, Any>>)

    override val args: List<Any>
        get() = listOf(torrentId, listOf<String>())
}
