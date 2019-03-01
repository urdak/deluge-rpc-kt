package net.ickis.deluge.event

/**
 * Emitted when a torrent changes state.
 */
object TorrentStateChangedEvent: DelugeEvent<TorrentStateChanged>("TorrentStateChangedEvent") {
    override fun createNotification(args: List<*>) = TorrentStateChanged(args.getArg(0), args.getArg(1))
}

/**
 * @param torrentId The id of the torrent.
 * @param state The new state of the torrent.
 */
data class TorrentStateChanged(val torrentId: String, val state: String)
