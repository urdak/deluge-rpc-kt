package net.ickis.deluge.event

/**
 * Emitted when a file completes.
 */
object TorrentFileCompletedEvent : DelugeEvent<TorrentFileCompleted>("TorrentFileCompletedEvent") {
    override fun createNotification(args: List<*>) = TorrentFileCompleted(args.getArg(0), args.getArg(1))
}

/**
 * @param torrentId The id of the torrent.
 * @param index The index of the file.
 */
data class TorrentFileCompleted(val torrentId: String, val index: Int)
