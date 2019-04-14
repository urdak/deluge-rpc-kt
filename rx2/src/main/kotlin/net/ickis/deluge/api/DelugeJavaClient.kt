package net.ickis.deluge.api

import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxSingle
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

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
    fun <T> request(request: Request<T>) = toSingleOpt { client.request(request) }

    /**
     * @see [DelugeClient.subscribe]
     */
    fun <T: Any> subscribe(event: DelugeEvent<T>): Observable<T> = runBlocking {
        val channel = client.subscribe(event)
        channel.asObservable(EmptyCoroutineContext).doOnDispose {
            channel.cancel()
        }
    }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(magnetLink: String) = toSingleOpt { client.addTorrent(magnetLink) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(url: URL) = toSingleOpt { client.addTorrent(url) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(path: Path) = toSingleOpt { client.addTorrent(path) }

    /**
     * @see [DelugeClient.removeTorrent]
     */
    fun removeTorrent(torrentId: String, removeData: Boolean) = toSingleOpt { client.removeTorrent(torrentId, removeData) }

    /**
     * @see [DelugeClient.getTorrentStatus]
     */
    fun getTorrentStatus(torrentId: String) = toSingle { client.getTorrentStatus(torrentId) }

    /**
     * @see [DelugeClient.getFreeSpace]
     */
    fun getFreeSpace() = toSingle { client.getFreeSpace() }

    private inline fun <T: Any> toSingle(crossinline block: suspend CoroutineScope.() -> T): Single<T> {
        return runBlocking { rxSingle { block() } }
    }

    private inline fun <T> toSingleOpt(crossinline block: suspend CoroutineScope.() -> T): Single<Optional<T>> {
        return runBlocking { rxSingle { Optional.ofNullable(block()) } }
    }
}
