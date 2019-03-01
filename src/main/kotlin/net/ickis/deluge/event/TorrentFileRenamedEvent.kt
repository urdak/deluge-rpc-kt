package net.ickis.deluge.event

/**
 * Emitted when a file within a torrent has been renamed.
 */
object TorrentFileRenamedEvent : DelugeEvent<TorrentFileRenamed>("TorrentFileRenamedEvent") {
    override fun createNotification(args: List<*>) = TorrentFileRenamed(args.getArg(0), args.getArg(1), args.getArg(2))
}

/**
 * @param torrentId The id of the torrent.
 * @param index The index of the file.
 * @param name The new name of the file.
 */
data class TorrentFileRenamed(val torrentId: String, val index: Int, val name: String)
