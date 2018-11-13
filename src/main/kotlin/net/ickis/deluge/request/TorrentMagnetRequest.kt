package net.ickis.deluge.request

internal class TorrentMagnetRequest(
        private val magnetLink: String
) : Request<String>("core.add_torrent_magnet") {
    override val args: List<Any>
        get() = listOf(magnetLink, emptyMap<String, String>())
}
