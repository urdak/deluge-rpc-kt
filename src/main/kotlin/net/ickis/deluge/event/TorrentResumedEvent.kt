package net.ickis.deluge.event

/**
 * Emitted when a torrent resumes from a paused state. The event emits a [String], which contains the id of the
 * torrent that was resumed.
 */
object TorrentResumedEvent : DelugeEvent<String>("TorrentResumedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
