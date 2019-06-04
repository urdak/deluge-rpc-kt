package net.ickis.deluge.api

import io.reactivex.Observable
import io.reactivex.Single
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.request.Request
import java.net.URL
import java.nio.file.Path
import java.util.*

interface DelugeJavaClient : AutoCloseable {
    /**
     * @see [DelugeClient.request]
     */
    fun <T> request(request: Request<T>): Single<Optional<T>>

    /**
     * @see [DelugeClient.subscribe]
     */
    fun <T: Any> subscribe(event: DelugeEvent<T>): Observable<T>

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(magnetLink: String): Single<Optional<String?>>

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(url: URL): Single<Optional<String?>>

    /**
     * @see [DelugeClient.addTorrent]
     */
    fun addTorrent(path: Path): Single<Optional<String?>>

    /**
     * @see [DelugeClient.removeTorrent]
     */
    fun removeTorrent(torrentId: String, removeData: Boolean): Single<Optional<Boolean?>>

    /**
     * @see [DelugeClient.getTorrentStatus]
     */
    fun getTorrentStatus(torrentId: String): Single<Torrent>

    /**
     * @see [DelugeClient.getFreeSpace]
     */
    fun getFreeSpace(): Single<Long>

    override fun close()
}
