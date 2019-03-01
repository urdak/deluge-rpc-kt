package net.ickis.deluge.event

/**
 * Emitted when a client disconnects. The event emits a [String], which contains the session id that has disconnected.
 */
object ClientDisconnectedEvent : DelugeEvent<String>("ClientDisconnectedEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
