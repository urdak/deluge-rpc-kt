package net.ickis.deluge

import kotlinx.coroutines.*
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.SocketFactory
import net.ickis.deluge.request.Request
import org.apache.logging.log4j.LogManager
import java.io.Closeable
import java.io.IOException

private val logger = LogManager.getLogger(DelugeSession::class.java)

/**
 * Creates a socket connection to the Deluge daemon. Sets up an actor [dispatcher] to keep track of Deluge events.
 */
internal class DelugeSession(
        private val socket: DelugeSocket
) : CoroutineScope, Closeable {
    constructor(address: String, port: Int)
            : this(DelugeSocket(SocketFactory.createSocket(address, port)))

    private val job = Job()
    override val coroutineContext = Dispatchers.Default + job + exceptionHandler(logger)
    /**
     * Maintains the status of active requests. Processes incoming and outgoing events.
     */
    private val dispatcher = dispatcher(socket)

    /**
     * Sets up a coroutine to process socket input and create [DelugeEvent.Incoming] for the [messageHandler].
     */
    init {
        launch {
            for (raw in socket.reader) {
                try {
                    dispatcher.send(DispatcherEvent.Incoming(DelugeResponse.create(raw)))
                } catch (ex: IOException) {
                    logger.error("Failed to create Deluge response from $raw", ex)
                }
            }
        }
    }

    /**
     * Creates a new request [DispatcherEvent.Outgoing] for the [dispatcher]. Suspends until the [dispatcher]
     * receives a [DispatcherEvent.Incoming] with the response for the specified request.
     */
    suspend fun <T> request(request: Request<T>): T {
        val outgoing = DispatcherEvent.Outgoing(request, CompletableDeferred(SupervisorJob(job)))
        dispatcher.send(outgoing)
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
