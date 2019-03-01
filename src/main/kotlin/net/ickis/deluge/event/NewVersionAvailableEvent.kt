package net.ickis.deluge.event

/**
 * Emitted when a more recent version of Deluge is available. The event emits a [String], which contains the new
 * version that is available.
 */
object NewVersionAvailableEvent : DelugeEvent<String>("NewVersionAvailableEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
