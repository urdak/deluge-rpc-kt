package net.ickis.deluge.event

/**
 * Emitted when a torrent finishes downloading.
 */
object TorrentFinishedEvent : DelugeEvent<String>("TorrentFinishedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
