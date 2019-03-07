package net.ickis.deluge.samples

import kotlinx.coroutines.runBlocking
import net.ickis.deluge.api.DelugeClient
import net.ickis.deluge.api.Torrent
import net.ickis.deluge.api.delugeClient
import java.lang.IllegalArgumentException

const val IP = "localhost"
const val PORT = 1234
const val USERNAME = "username"
const val PASSWORD = "password"
const val MAGNET_LINK = ""

@Suppress("UNUSED_VARIABLE")
fun main() {
    runBlocking {
        delugeClient(IP, PORT, USERNAME, PASSWORD) { client ->
            val torrentId: String = client.addTorrent(MAGNET_LINK) ?: throw IllegalArgumentException("Bad magnet link")
            val torrent: Torrent = client.getTorrentStatus(torrentId)
            val removed: Boolean? = client.removeTorrent(torrentId, true)
        }
    }
}

fun <T> simpleClient(block: suspend (DelugeClient) -> T) = runBlocking {
    delugeClient(IP, PORT, USERNAME, PASSWORD, block)
}
