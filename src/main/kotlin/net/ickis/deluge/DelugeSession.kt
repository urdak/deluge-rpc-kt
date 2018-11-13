package net.ickis.deluge

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.SocketFactory
import net.ickis.deluge.request.Request
import java.io.Closeable
import java.io.IOException
import java.util.*

internal class DelugeSession(
        private val socket: DelugeSocket
) : CoroutineScope, Closeable {
    constructor(address: String, port: Int = 58846)
            : this(DelugeSocket(SocketFactory.createSocket(address, port)))

    private val job = Job()
    override val coroutineContext = Dispatchers.Default + job
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

    init {
        launch {
            for (raw in socket.inputProcessor) {
                try {
                    messageHandler.send(DelugeEvent.Incoming(DelugeResponse.create(raw.data)))
                } catch (ex: IOException) {
                    // TODO: handle Response::create here?
                }
            }
        }
    }

    suspend fun <T> request(request: Request<T>): T {
        val outgoing = DelugeEvent.Outgoing(request, CompletableDeferred(SupervisorJob(job)))
        messageHandler.send(outgoing)
        return outgoing.deferred.await()
    }

    override fun close() {
        job.cancel()
        socket.close()
    }
}
