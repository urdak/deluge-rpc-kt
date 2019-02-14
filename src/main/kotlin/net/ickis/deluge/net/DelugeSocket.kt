package net.ickis.deluge.net

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream
import java.io.Closeable
import java.io.IOException
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import javax.net.ssl.SSLSocket

/**
 * Writes [RawRequest]'s to the [SSLSocket] in the expected format.
 * Reads [RawResponse]'s from the [SSLSocket] in the expected format.
 */
internal class DelugeSocket(private val socket: SSLSocket) : CoroutineScope, Closeable {
    private var closed = false
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job
    private val writeChannel: Channel<RawRequest> by lazy {
        val channel = Channel<RawRequest>(50)
        launch {
            for (request in channel) {
                try {
                    val outputStream = RencodeOutputStream(
                            DeflaterOutputStream(socket.outputStream, true))
                    outputStream.writeList(request.data)
                    outputStream.flush()
                } catch (ex: IOException) {
                    // TODO: log
                    if (!closed) {
                        throw ex
                    }
                }
            }
        }
        channel
    }
    /**
     * Channel that contains incoming responses.
     */
    internal val inputProcessor: ReceiveChannel<RawResponse> by lazy {
        produce {
            while (isActive) {
                try {
                    val inputStream = RencodeInputStream(InflaterInputStream(socket.inputStream))
                    send(RawResponse(inputStream.readList()))
                } catch (ex: IOException) {
                    // TODO: log
                    if (!closed) {
                        throw ex
                    }
                }
            }
        }
    }

    /**
     * Writes the [RawRequest] to the socket.
     */
    suspend fun write(rawRequest: RawRequest) {
        writeChannel.send(rawRequest)
    }

    override fun close() {
        closed = true
        writeChannel.cancel()
        job.cancel()
        socket.close()
    }
}

internal data class RawRequest(val data: List<List<Any>>)

internal data class RawResponse(val data: List<*>)
