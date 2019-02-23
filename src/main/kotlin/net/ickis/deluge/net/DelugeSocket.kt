package net.ickis.deluge.net

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import net.ickis.deluge.exceptionHandler
import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.Closeable
import java.io.IOException
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import javax.net.ssl.SSLSocket

private val logger: Logger = LogManager.getLogger()

/**
 * Writes [RawRequest]'s to the [SSLSocket] in the expected format.
 * Reads [RawResponse]'s from the [SSLSocket] in the expected format.
 */
internal class DelugeSocket(private val socket: SSLSocket) : CoroutineScope, Closeable {
    private var closed = false
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job + exceptionHandler(logger)
    private val writer: Channel<RawRequest> by lazy { socketWriter() }
    val reader: ReceiveChannel<RawResponse> by lazy { socketReader() }

    /**
     * Writes the [RawRequest] to the socket.
     */
    suspend fun write(rawRequest: RawRequest) {
        writer.send(rawRequest)
    }

    override fun close() {
        closed = true
        writer.cancel()
        reader.cancel()
        job.cancel()
        socket.close()
    }

    /**
     * Creates a channel that can be written into to send encoded data via the [SSLSocket].
     */
    private fun CoroutineScope.socketWriter(): Channel<RawRequest> {
        val channel = Channel<RawRequest>(50)
        launch {
            for (request in channel) {
                try {
                    val outputStream = RencodeOutputStream(DeflaterOutputStream(socket.outputStream, true))
                    outputStream.writeList(request.data)
                    outputStream.flush()
                } catch (ex: IOException) {
                    if (!closed) {
                        throw ex
                    }
                }
            }
        }
        return channel
    }

    /**
     * Creates a channel that can be read from to receive decoded responses from the [SSLSocket].
     */
    private fun CoroutineScope.socketReader(): ReceiveChannel<RawResponse> = produce {
        while (isActive) {
            try {
                val inputStream = RencodeInputStream(InflaterInputStream(socket.inputStream))
                send(RawResponse(inputStream.readList()))
            } catch (ex: IOException) {
                if (!closed) {
                    throw ex
                }
            }
        }
    }
}

internal data class RawRequest(val data: List<List<Any>>)

internal data class RawResponse(val data: List<*>)
