package net.ickis.deluge.event

/**
 * Emitted when a torrent has been removed from the session. The event emits a [String], which contains the id of the
 * torrent that was removed.
 */
object TorrentRemovedEvent : DelugeEvent<String>("TorrentRemovedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
