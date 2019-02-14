package net.ickis.deluge

/**
 * Contains information about the exception that was thrown by the daemon.
 * @param type Type of the exception thrown by the daemon.
 * @param backtrace Backtrace of the exception thrown by the daemon.
 */
class DelugeException internal constructor(
        val type: String? = DelugeException::class.java.simpleName,
        message: String?,
        val backtrace: String? = ""
) : Exception(message) {
    override fun toString(): String {
        val exType = type ?: javaClass.name
        return "$exType: ${message.wrapOrEmpty(suffix = " ")}${backtrace.wrapOrEmpty("\n")}"
    }

    private fun String?.wrapOrEmpty(prefix: String = "", suffix: String = "") =
            if (isNullOrBlank()) "" else prefix + this + suffix
}
