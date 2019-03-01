package net.ickis.deluge.event

/**
 * Emitted when a torrent is about to be removed from the session.
 * TODO: inline class for single parameter notifications?
 */
object PreTorrentRemovedEvent : DelugeEvent<String>("PreTorrentRemovedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
