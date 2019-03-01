package net.ickis.deluge.event

/**
 * Emitted when the storage location for a torrent has been moved.
 */
object TorrentStorageMovedEvent : DelugeEvent<TorrentStorageMoved>("TorrentStorageMovedEvent") {
    override fun createNotification(args: List<*>) = TorrentStorageMoved(args.getArg(0), args.getArg(1))
}

/**
 * @param torrentId The id of the torrent.
 * @param path The new location of the torrent.
 */
data class TorrentStorageMoved(val torrentId: String, val path: String)
