package net.ickis.deluge.event

/**
 * Event that is generated by the daemon and is emitted to the client.
 * @param T The type of the notification that is emitted by the event.
 */
abstract class DelugeEvent<T>(val name: String) {
    /**
     * Creates a notification from the parameters received from the daemon.
     */
    abstract fun createNotification(args: List<*>): T

    override fun toString(): String {
        return "DelugeEvent(name=\"$name\")"
    }
}

/**
 * Event that does not contain any data.
 */
abstract class EmptyEvent(name: String): DelugeEvent<Unit>(name) {
    override fun createNotification(args: List<*>) = Unit
}

/**
 * Helper method for getting notification fields from the argument list.
 */
inline fun <reified T> List<*>.getArg(idx: Int): T {
    if (idx >= size) IllegalArgumentException()
    return get(idx).let { it as? T ?: throw IllegalArgumentException("Expected ${T::class.java} at $idx, but got $it") }
}
