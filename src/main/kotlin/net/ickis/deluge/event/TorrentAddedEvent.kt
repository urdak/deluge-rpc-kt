package net.ickis.deluge.event

/**
 * Emitted when a new torrent is successfully added to the session. The event emits a [String], which contains the id of
 * the torrent that was added.
 */
object TorrentAddedEvent : DelugeEvent<String>("TorrentAddedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
