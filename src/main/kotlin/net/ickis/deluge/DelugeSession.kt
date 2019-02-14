package net.ickis.deluge

import kotlinx.coroutines.*
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.SocketFactory
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.io.IOException

/**
 * Creates a socket connection to the Deluge daemon. Sets up an actor [eventHandler] to keep track of Deluge events.
 */
internal class DelugeSession(
        private val socket: DelugeSocket
) : CoroutineScope, Closeable {
    constructor(address: String, port: Int)
            : this(DelugeSocket(SocketFactory.createSocket(address, port)))

    private val job = Job()
    override val coroutineContext = Dispatchers.Default + job
    /**
     * Maintains the status of active requests. Processes incoming and outgoing deluge events.
     */
    private val eventHandler = eventHandler(socket)

    /**
     * Sets up a coroutine to process socket input and create [DelugeEvent.Incoming] for the [messageHandler].
     */
    init {
        launch {
            for (raw in socket.reader) {
                try {
                    eventHandler.send(DelugeEvent.Incoming(DelugeResponse.create(raw)))
                } catch (ex: IOException) {
                    // TODO: handle Response::create here?
                }
            }
        }
    }

    /**
     * Creates a new request [DelugeEvent.Outgoing] for the [eventHandler]. Suspends until the [eventHandler]
     * receives a [DelugeEvent.Incoming] with the response for the specified request.
     */
    suspend fun <T> request(request: Request<T>): T {
        val outgoing = DelugeEvent.Outgoing(request, CompletableDeferred(SupervisorJob(job)))
        eventHandler.send(outgoing)
        return outgoing.deferred.await()
    }

    /**
     * Cancels all coroutines and closes the socket.
     */
    override fun close() {
        job.cancel()
        socket.close()
    }
}
