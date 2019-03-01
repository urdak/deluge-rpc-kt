package net.ickis.deluge.event

/**
 * Emitted when the external ip address is received from libtorrent. The event emits a [String], which contains the ip
 * address.
 */
object ExternalIPEvent : DelugeEvent<String>("ExternalIPEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
