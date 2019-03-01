package net.ickis.deluge.event

/**
 * Emitted when a session has started. This typically only happens once when the daemon is initially started.
 */
object SessionStartedEvent : EmptyEvent("SessionStartedEvent")
