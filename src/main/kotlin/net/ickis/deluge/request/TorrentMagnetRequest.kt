package net.ickis.deluge.request

/**
 * Adds a torrent using the specified [magnetLink]
 */
class TorrentMagnetRequest(
        private val magnetLink: String
) : SimpleRequest<String?>("core.add_torrent_magnet") {
    override val args: List<Any>
        get() = listOf(magnetLink, emptyMap<String, String>())
}
