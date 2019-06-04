package net.ickis.deluge.api

import kotlinx.coroutines.channels.ReceiveChannel
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.request.*
import java.io.Closeable
import java.net.URL
import java.nio.file.Path

/**
 * Provides a suspending API for basic Deluge RPC commands. If the Deluge RPC returns an exception, the exception is
 * wrapped into a [net.ickis.deluge.DelugeException] exception and thrown to the caller.
 */
interface DelugeClient : Closeable {
    /**
     * Sends a [Request] to the Deluge daemon and suspends until a reply is received and processed by the client.
     * Additional requests can be added to the client referencing the <a href="https://deluge.readthedocs.io/en/develop/reference/api.html">Deluge RPC API</a>.
     */
    suspend fun <T> request(request: Request<T>): T

    /**
     * Subscribes to an event, which is emitted by the daemon. The caller is responsible for closing the [ReceiveChannel].
     * @param T The type of the notification emitted by the channel.
     * @return [ReceiveChannel] that emits notifications specified by the [DelugeEvent]
     */
    suspend fun <T> subscribe(event: DelugeEvent<T>): ReceiveChannel<T>
}

/**
 * Adds a torrent using the provided [magnetLink].
 * @return id of the torrent.
 */
suspend fun DelugeClient.addTorrent(magnetLink: String): String? = request(TorrentMagnetRequest(magnetLink))

/**
 * Adds a torrent using the provided [url] of the torrent file.
 * @return id of the torrent.
 */
suspend fun DelugeClient.addTorrent(url: URL): String? = request(TorrentURLRequest(url))

/**
 * Adds a torrent using the provided local [path] of the torrent file.
 * @return id of the torrent.
 */
suspend fun DelugeClient.addTorrent(path: Path): String? = request(TorrentPathRequest(path))

/**
 * Removes the torrent with the specified [torrentId]. Removes the data based on the value of [removeData].
 * @return whether the torrent was successfully removed or not.
 */
suspend fun DelugeClient.removeTorrent(torrentId: String, removeData: Boolean): Boolean? = request(
        RemoveTorrents(torrentId, removeData))

/**
 * Retrieves the status of the torrent with the specified [torrentId].
 * @return the information about the torrent.
 * @see [Torrent]
 */
suspend fun DelugeClient.getTorrentStatus(torrentId: String): Torrent = request(TorrentStatus(torrentId))

/**
 * Retrieves the number of free bytes in the default download location.
 */
suspend fun DelugeClient.getFreeSpace(): Long = request(FreeSpace)
