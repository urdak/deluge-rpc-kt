package net.ickis.deluge.request

import java.net.URL

/**
 * Adds a torrent using the specified [url] of the torrent file.
 */
class TorrentURLRequest(
        private val url: URL
) : SimpleRequest<String>("core.add_torrent_url") {
    override val args: List<Any>
        get() = listOf(url.toString(), emptyMap<String, String>())
}
