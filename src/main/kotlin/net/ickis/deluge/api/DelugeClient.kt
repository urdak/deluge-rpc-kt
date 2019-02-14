package net.ickis.deluge.api

import kotlinx.coroutines.runBlocking
import net.ickis.deluge.DelugeSession
import net.ickis.deluge.request.*
import java.io.Closeable
import java.net.URL
import java.nio.file.Path

/**
 * The port that is used by default by the [DelugeClient].
 */
const val DEFAULT_DELUGE_PORT = 58846

/**
 * Provides a suspending API for basic Deluge RPC commands. If the Deluge RPC returns an exception, the exception is
 * wrapped into a [net.ickis.deluge.DelugeException] exception and thrown to the caller.
 */
class DelugeClient private constructor(
        private val session: DelugeSession
) : Closeable by session {
    /**
     * Establishes a connection to the Deluge daemon running on the specified address:port using the specified
     * credentials. The user is responsible for closing the client. Consider using [delugeClient], which automatically
     * performs cleanup.
     */
    constructor(address: String,
                port: Int = DEFAULT_DELUGE_PORT,
                username: String,
                password: String
    ) : this(DelugeSession(address, port)) {
        runBlocking { session.request(LoginRequest(username, password)) }
    }

    /**
     * Sends a [Request] to the Deluge daemon and suspends until a reply is received and processed by the client.
     * Additional requests can be added to the client referencing the <a href="https://deluge.readthedocs.io/en/develop/reference/api.html">Deluge RPC API</a>.
     */
    suspend fun <T> request(request: Request<T>): T {
        return session.request(request)
    }

    /**
     * Adds a torrent using the provided [magnetLink].
     * @return id of the torrent.
     */
    suspend fun addTorrent(magnetLink: String): String = request(TorrentMagnetRequest(magnetLink))

    /**
     * Adds a torrent using the provided [url] of the torrent file.
     * @return id of the torrent.
     */
    suspend fun addTorrent(url: URL): String = request(TorrentURLRequest(url))

    /**
     * Adds a torrent using the provided local [path] of the torrent file.
     * @return id of the torrent.
     */
    suspend fun addTorrent(path: Path): String = request(TorrentPathRequest(path))

    /**
     * Removes the torrent with the specified [torrentId]. Removes the data based on the value of [removeData].
     * @return whether the torrent was successfully removed or not.
     */
    suspend fun removeTorrent(torrentId: String, removeData: Boolean): Boolean? = request(
            RemoveTorrents(torrentId, removeData))

    /**
     * Retrieves the status of the torrent with the specified [torrentId].
     * @return the information about the torrent.
     * @see [Torrent]
     */
    suspend fun getTorrentStatus(torrentId: String): Torrent {
        val map = request(TorrentStatus(torrentId))
        return Torrent(map)
    }

    /**
     * Retrieves the number of free bytes in the default download location.
     */
    suspend fun getFreeSpace(): Long = request(FreeSpace)
}

/**
 * Creates a [DelugeClient] and performs the specified [block]. Closes the connection once the [block] is complete.
 */
suspend fun <T> delugeClient(address: String,
                             port: Int = DEFAULT_DELUGE_PORT,
                             username: String,
                             password: String,
                             block: suspend (DelugeClient) -> T) = DelugeClient(address, port, username, password)
        .use { block(it) }
