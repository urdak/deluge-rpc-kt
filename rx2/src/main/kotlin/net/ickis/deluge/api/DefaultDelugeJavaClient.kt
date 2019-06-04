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
class DefaultDelugeJavaClient private constructor(
        private val client: DelugeClient
) : Closeable by client, DelugeJavaClient {
    @JvmOverloads
    constructor(address: String,
                port: Int = DEFAULT_DELUGE_PORT,
                username: String,
                password: String
    ) : this(DefaultDelugeClient(address, port, username, password))

    /**
     * @see [DelugeClient.request]
     */
    override fun <T> request(request: Request<T>) = toSingleOpt { client.request(request) }

    /**
     * @see [DelugeClient.subscribe]
     */
    override fun <T: Any> subscribe(event: DelugeEvent<T>): Observable<T> = runBlocking {
        val channel = client.subscribe(event)
        channel.asObservable(EmptyCoroutineContext).doOnDispose {
            channel.cancel()
        }
    }

    /**
     * @see [DelugeClient.addTorrent]
     */
    override fun addTorrent(magnetLink: String) = toSingleOpt { client.addTorrent(magnetLink) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    override fun addTorrent(url: URL) = toSingleOpt { client.addTorrent(url) }

    /**
     * @see [DelugeClient.addTorrent]
     */
    override fun addTorrent(path: Path) = toSingleOpt { client.addTorrent(path) }

    /**
     * @see [DelugeClient.removeTorrent]
     */
    override fun removeTorrent(torrentId: String, removeData: Boolean) = toSingleOpt { client.removeTorrent(torrentId, removeData) }

    /**
     * @see [DelugeClient.getTorrentStatus]
     */
    override fun getTorrentStatus(torrentId: String) = toSingle { client.getTorrentStatus(torrentId) }

    /**
     * @see [DelugeClient.getFreeSpace]
     */
    override fun getFreeSpace() = toSingle { client.getFreeSpace() }

    private inline fun <T: Any> toSingle(crossinline block: suspend CoroutineScope.() -> T): Single<T> {
        return runBlocking { rxSingle { block() } }
    }

    private inline fun <T> toSingleOpt(crossinline block: suspend CoroutineScope.() -> T): Single<Optional<T>> {
        return runBlocking { rxSingle { Optional.ofNullable(block()) } }
    }
}
