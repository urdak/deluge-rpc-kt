package net.ickis.deluge.net

import io.mockk.*
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.request.LoginRequest
import net.ickis.rencode.RencodeInputStream
import net.ickis.rencode.RencodeOutputStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import java.util.zip.ZipException
import javax.net.ssl.SSLSocket

class DelugeSocketTest {
    private lateinit var serverOutput: PipedOutputStream
    private lateinit var inputStream: PipedInputStream
    private lateinit var outputStream: ByteArrayOutputStream
    private val sslSocket: SSLSocket = mockk()

    @BeforeEach
    fun before() {
        serverOutput = PipedOutputStream()
        inputStream = PipedInputStream(serverOutput)
        outputStream = ByteArrayOutputStream()
        clearMocks(sslSocket)
        coEvery { sslSocket.outputStream } returns outputStream
        coEvery { sslSocket.inputStream } returns inputStream
        every { sslSocket.close() } answers {
            inputStream.close()
            outputStream.close()
            serverOutput.close()
        }
    }

    @Test
    fun `Write data to the socket`() = DelugeSocket(sslSocket).use { socket ->
        val request = LoginRequest("user1", "password1").serialize(1)
        runBlocking {
            socket.write(RawRequest(request))
        }
        while (outputStream.size() < 40) {
        }
        RencodeInputStream(
                InflaterInputStream(ByteArrayInputStream(outputStream.toByteArray()))).use {
            Assertions.assertEquals(request, it.readList())
        }
    }.also {
        verifySequence {
            sslSocket.outputStream
            sslSocket.close()
        }
    }

    @Test
    fun `Read data from the socket`() = DelugeSocket(sslSocket).use { socket ->
        val rawResponse = RawResponse(listOf(1, 1, 5))
        val baos = ByteArrayOutputStream()
        RencodeOutputStream(DeflaterOutputStream(baos, true)).use {
            it.writeList(rawResponse.data)
            it.flush()
        }
        serverOutput.write(baos.toByteArray())
        val response = runBlocking {
            socket.inputProcessor.receive()
        }
        Assertions.assertEquals(rawResponse, response)
    }.also {
        verify(atLeast = 1, atMost = 2) {
            sslSocket.inputStream
        }
        verify {
            sslSocket.close()
        }
    }

    @Test
    fun `Error during socket read`() = DelugeSocket(sslSocket).use { socket ->
        serverOutput.write(1)
        serverOutput.write(2)
        Assertions.assertThrows(ZipException::class.java) {
            runBlocking {
                socket.inputProcessor.receive()
            }
        }
    }.let {
        sslSocket.inputStream
        sslSocket.close()
    }
}
