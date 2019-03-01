package net.ickis.deluge.event

/**
 * Emitted when a folder within a torrent has been renamed.
 */
object TorrentFolderRenamedEvent : DelugeEvent<TorrentFolderRenamed>("TorrentFolderRenamedEvent") {
    override fun createNotification(args: List<*>)
            = TorrentFolderRenamed(args.getArg(0), args.getArg(1), args.getArg(2))
}

/**
 * @param torrentId The id of the torrent.
 * @param oldFolder The name of the old folder.
 * @param newFolder The name of the new folder.
 */
data class TorrentFolderRenamed(val torrentId: String, val oldFolder: String, val newFolder: String)
