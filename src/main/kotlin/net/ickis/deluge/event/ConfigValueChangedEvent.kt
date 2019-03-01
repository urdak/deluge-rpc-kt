package net.ickis.deluge.event

/**
 * Emitted when a config value changes in the Core.
 */
object ConfigValueChangedEvent : DelugeEvent<ConfigValueChanged>("ConfigValueChangedEvent") {
    override fun createNotification(args: List<*>) = ConfigValueChanged(args.getArg(0), args.getArg(1))
}

/**
 * @param key The key that changed.
 * @param value The new value of the config parameter.
 */
data class ConfigValueChanged(val key: String, val value: String)
