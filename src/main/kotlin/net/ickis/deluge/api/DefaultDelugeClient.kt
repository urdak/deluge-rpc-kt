package net.ickis.deluge.api

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.DelugeSession
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.request.LoginRequest
import net.ickis.deluge.request.Request
import java.io.Closeable

/**
 * The port that is used by default by the [DefaultDelugeClient].
 */
const val DEFAULT_DELUGE_PORT = 58846

/**
 * Default implementation for [DelugeClient].
 */
class DefaultDelugeClient private constructor(
        private val session: DelugeSession
) : DelugeClient, Closeable by session {
    /**
     * Establishes a connection to the Deluge daemon running on the specified address:port using the specified
     * credentials. The user is responsible for closing the client. Consider using [delugeClient], which automatically
     * performs cleanup.
     */
    constructor(address: String,
                port: Int = DEFAULT_DELUGE_PORT,
                username: String,
                password: String
    ) : this(DelugeSession(address, port)) {
        runBlocking { session.request(LoginRequest(username, password)) }
    }

    /**
     * @see [DelugeClient.request]
     */
    override suspend fun <T> request(request: Request<T>): T = session.request(request)

    /**
     * @see [DelugeClient.subscribe]
     */
    override suspend fun <T> subscribe(event: DelugeEvent<T>): ReceiveChannel<T> = session.subscribe(event)
}

/**
 * Creates a [DefaultDelugeClient] and performs the specified [block].
 * Closes the connection once the [block] is complete.
 */
suspend fun <T> delugeClient(address: String,
                             port: Int = DEFAULT_DELUGE_PORT,
                             username: String,
                             password: String,
                             block: suspend (DefaultDelugeClient) -> T) = DefaultDelugeClient(address, port, username, password)
        .use { block(it) }
