package net.ickis.deluge.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * @see [DelugeClient]
 */
class DelugeJavaClient private constructor(private val client: DelugeClient) : Closeable by client {
    @JvmOverloads
    constructor(address: String,
                port: Int = DEFAULT_DELUGE_PORT,
                username: String,
                password: String
    ) : this(DelugeClient(address, port, username, password))

    /**
     * @see [DelugeClient.request]
     */
    fun <T> request(request: Request<T>): CompletableFuture<T> = blockingFuture { client.request(request) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(magnetLink: String) = blockingFuture { client.addTorrent(magnetLink) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(url: URL) = blockingFuture { client.addTorrent(url) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(path: Path) = blockingFuture { client.addTorrent(path) }

    /**
     * @see [DelugeClient.removeTorrent]
     */
    fun removeTorrent(torrentId: String, removeData: Boolean) = blockingFuture { client.removeTorrent(torrentId, removeData) }

    /**
     * @see [DelugeClient.getTorrentStatus]
     */
    fun getTorrentStatus(torrentId: String) = blockingFuture { client.getTorrentStatus(torrentId) }

    /**
     * @see [DelugeClient.getFreeSpace]
     */
    fun getFreeSpace() = blockingFuture { client.getFreeSpace() }

    private inline fun <T> blockingFuture(crossinline block: suspend CoroutineScope.() -> T) = runBlocking { future { block() } }
}
