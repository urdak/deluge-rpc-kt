package net.ickis.deluge.event

/**
 * Emitted when a plugin is enabled in the Core. The event emits a [String], which contains the name of the plugin that
 * has been enabled.
 */
object PluginEnabledEvent : DelugeEvent<String>("PluginEnabledEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
