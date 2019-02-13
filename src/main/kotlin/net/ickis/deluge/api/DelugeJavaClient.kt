package net.ickis.deluge.api

import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.DelugeSession
import net.ickis.deluge.request.*
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class DelugeJavaClient private constructor(
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

    fun <T> request(request: Request<T>): CompletableFuture<T> = runBlocking { future { session.request(request) } }
    fun addTorrent(magnetLink: String) = request(TorrentMagnetRequest(magnetLink))
    fun addTorrent(url: URL) = request(TorrentURLRequest(url))
    fun addTorrent(path: Path) = request(TorrentPathRequest(path))
    fun removeTorrent(torrentId: String, removeData: Boolean) = request(RemoveTorrents(torrentId, removeData))
    fun getTorrentStatus(torrentId: String) = request(TorrentStatus(torrentId)).thenApply(::Torrent)!!
    fun getFreeSpace() = request(FreeSpace)
}
