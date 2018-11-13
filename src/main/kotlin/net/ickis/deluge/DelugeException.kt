package net.ickis.deluge

class DelugeException(val type: String? = DelugeException::class.java.simpleName,
                      message: String?,
                      val backtrace: String? = "") : Exception(message) {
    override fun toString(): String {
        val exType = type ?: javaClass.name
        return "$exType: ${message.wrapOrEmpty(suffix = " ")}${backtrace.wrapOrEmpty("\n")}"
    }

    private fun String?.wrapOrEmpty(prefix: String = "", suffix: String = "") =
            if (isNullOrBlank()) "" else prefix + this + suffix
}
