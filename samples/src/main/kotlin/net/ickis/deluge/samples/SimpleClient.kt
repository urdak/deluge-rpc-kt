package net.ickis.deluge.samples

import kotlinx.coroutines.runBlocking
import net.ickis.deluge.api.DelugeClient
import net.ickis.deluge.api.Torrent

@Suppress("UNUSED_VARIABLE")
fun main() {
    val ip = "localhost"
    val port = 1234
    val username = "username"
    val password = "password"
    val magnetLink = ""
    runBlocking {
        DelugeClient(ip, port, username, password).use { client ->
            val torrentId: String = client.addTorrent(magnetLink)
            val torrent: Torrent = client.getTorrentStatus(torrentId)
            val removed: Boolean? = client.removeTorrent(torrentId, true)
        }
    }
}
