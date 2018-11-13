package net.ickis.deluge.request

import java.net.URL

internal class TorrentURLRequest(
        private val url: URL
) : Request<String>("core.add_torrent_url") {
    override val args: List<Any>
        get() = listOf(url.toString(), emptyMap<String, String>())
}
