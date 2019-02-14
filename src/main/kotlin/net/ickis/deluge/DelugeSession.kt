package net.ickis.deluge

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.SocketFactory
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.io.IOException
import java.util.*

/**
 * Creates a socket connection to the Deluge daemon. Sets up an actor [messageHandler] to keep track of Deluge events.
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
    private val messageHandler = actor<DelugeEvent> {
        val requests = HashMap<Int, CompletableDeferred<*>>()
        var counter = 0
        for (event in channel) {
            when (event) {
                is DelugeEvent.Outgoing<*> -> {
                    val id = counter++
                    requests[id] = event.deferred
                    socket.write(event.serialize(id))
                }
                is DelugeEvent.Incoming -> {
                    val request = requests.remove(event.response.requestId)
                    if (request != null) {
                        event.process(request)
                    } else {
                        TODO("LOG ME")
                    }
                }
            }
        }
    }

    /**
     * Sets up a coroutine to process socket input and create [DelugeEvent.Incoming] for the [messageHandler].
     */
    init {
        launch {
            for (raw in socket.inputProcessor) {
                try {
                    messageHandler.send(DelugeEvent.Incoming(DelugeResponse.create(raw)))
                } catch (ex: IOException) {
                    // TODO: handle Response::create here?
                }
            }
        }
    }

    /**
     * Creates a new request [DelugeEvent.Outgoing] for the [messageHandler]. Suspends until the [messageHandler]
     * receives a [DelugeEvent.Incoming] with the response for the specified request.
     */
    suspend fun <T> request(request: Request<T>): T {
        val outgoing = DelugeEvent.Outgoing(request, CompletableDeferred(SupervisorJob(job)))
        messageHandler.send(outgoing)
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
