package net.ickis.deluge

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import mu.KotlinLogging
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.SocketFactory
import net.ickis.deluge.request.EventRequest
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.io.IOException

private val logger = KotlinLogging.logger {}

/**
 * Creates a socket connection to the Deluge daemon. Sets up an actor [dispatcher] that processes the daemon data.
 */
internal class DelugeSession(
        private val socket: DelugeSocket
) : CoroutineScope, Closeable {
    constructor(address: String, port: Int)
            : this(DelugeSocket(SocketFactory.createSocket(address, port)))

    private val job = Job()
    override val coroutineContext = Dispatchers.Default + job + exceptionHandler(logger)
    /**
     * Maintains the status of active requests.
     */
    private val dispatcher = dispatcher(socket)

    /**
     * Sets up a coroutine to process socket input and create dispatcher receive commands.
     */
    init {
        launch {
            for (raw in socket.reader) {
                try {
                    dispatcher.send(Receive(DelugeResponse.create(raw)))
                } catch (ex: IOException) {
                    logger.error("Failed to create Deluge response from $raw", ex)
                }
            }
        }
    }

    /**
     * Creates a new request [Send] for the [dispatcher]. Suspends until the [dispatcher]
     * receives a [Receive] with the response for the specified request.
     */
    suspend fun <T> request(request: Request<T>): T {
        val outgoing = Send(request, CompletableDeferred(SupervisorJob(job)))
        dispatcher.send(outgoing)
        return outgoing.deferred.await()
    }

    /**
     * Sends an [EventRequest] using [request] and, if the request is successful, sends a [Subscribe]
     * to the [dispatcher]. Provides a channel that can be consumed to receive notifications from the subscription.
     */
    suspend fun <T> subscribe(event: DelugeEvent<T>): ReceiveChannel<T> {
        val eventRequest = EventRequest(event)
        val successful = request(eventRequest)
        if (!successful) throw DelugeException(message = "Failed to subscribe to event ${event.name}")
        val subscription = Subscribe(eventRequest, Channel<T>(Channel.UNLIMITED))
        dispatcher.send(subscription)
        return subscription.channel
    }

    /**
     * Cancels all coroutines and closes the socket.
     */
    override fun close() {
        job.cancel()
        socket.close()
    }
}
