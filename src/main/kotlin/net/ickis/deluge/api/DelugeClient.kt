package net.ickis.deluge.api

import kotlinx.coroutines.runBlocking
import net.ickis.deluge.DelugeSession
import net.ickis.deluge.request.*
import java.io.Closeable
import java.net.URL
import java.nio.file.Path

class DelugeClient private constructor(
        private val session: DelugeSession
) : Closeable by session {
    @JvmOverloads
    constructor(address: String,
                port: Int = 58846,
                username: String,
                password: String
    ) : this(DelugeSession(address, port)) {
        runBlocking { session.request(LoginRequest(username, password)) }
    }

    suspend fun <T> request(request: Request<T>): T {
        return session.request(request)
    }

    suspend fun addTorrent(magnetLink: String) = request(TorrentMagnetRequest(magnetLink))

    suspend fun addTorrent(url: URL) = request(TorrentURLRequest(url))

    suspend fun addTorrent(path: Path) = request(TorrentPathRequest(path))

    suspend fun removeTorrent(torrentId: String, removeData: Boolean) = request(
            RemoveTorrents(torrentId, removeData))

    suspend fun getTorrentStatus(torrentId: String): Torrent {
        val map = request(TorrentStatus(torrentId))
        return Torrent(map)
    }

    suspend fun getFreeSpace() = request(FreeSpace)
}
