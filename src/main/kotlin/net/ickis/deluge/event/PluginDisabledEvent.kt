package net.ickis.deluge.event

/**
 * Emitted when a plugin is disabled in the Core. The event emits a [String], which contains the name of the plugin that
 * has been disabled.
 */
object PluginDisabledEvent : DelugeEvent<String>("PluginDisabledEvent") {
    override fun createNotification(args: List<*>) = args.getArg<String>(0)
}
