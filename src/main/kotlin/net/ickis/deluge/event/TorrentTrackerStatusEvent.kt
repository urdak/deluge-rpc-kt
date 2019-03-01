package net.ickis.deluge.event

/**
 * Emitted when a torrents tracker status changes.
 */
object TorrentTrackerStatusEvent : DelugeEvent<TorrentTrackerStatus>("TorrentTrackerStatusEvent") {
    override fun createNotification(args: List<*>) = TorrentTrackerStatus(args.getArg(0), args.getArg(1))
}

/**
 * @param torrentId The id of the torrent.
 * @param status The new status of the tracker
 */
data class TorrentTrackerStatus(val torrentId: String, val status: String)
