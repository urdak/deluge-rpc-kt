package net.ickis.deluge

import io.mockk.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.RawResponse
import net.ickis.deluge.net.RawRequest
import net.ickis.deluge.request.Request
import net.ickis.deluge.request.SimpleRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class DelugeSessionTest {
    private val socket: DelugeSocket = mockk()
    private val testChan = Channel<RawResponse>()
    private val inputProcessorMock = GlobalScope.produce {
        for (value in testChan) send(value)
    }

    class TestRequest<T>(method: String,
                         override val args: List<Any>,
                         override val kwargs: Map<Any, Any>
    ) : SimpleRequest<T>(method)

    @Test
    fun `Verify that all message types are parsed and the session stays alive`() {
        val request1 = TestRequest<Int>("test1", listOf("arg1"), mapOf(1 to 2))
        val request2 = TestRequest<String>("test2", listOf("arg2"), mapOf(3 to 4))
        val request3 = TestRequest<Boolean>("test3", listOf("arg3"), mapOf(5 to 6))
        // TODO: handle event messages
        val response1 = RawResponse(listOf(1, 0, 9))
        val exType = "TestType"
        val exMsg = "Oops!"
        val exBacktrace = "backtrace1\nbacktrace2"
        val response2 = RawResponse(listOf(2, 1, listOf(exType, exMsg, exBacktrace)))
        val response3 = RawResponse(listOf(1, 2, true))
        every { socket.reader } returns inputProcessorMock
        coEvery {
            socket.write(RawRequest(request1.serialize(0)))
        } coAnswers { testChan.send(response1) }
        coEvery {
            socket.write(RawRequest(request2.serialize(1)))
        } coAnswers { testChan.send(response2) }
        coEvery {
            socket.write(RawRequest(request3.serialize(2)))
        } coAnswers { testChan.send(response3) }
        every { socket.close() } just Runs
        DelugeSession(socket).use { session ->
            Assertions.assertEquals(response1.data[2],
                    runBlocking { session.request(request1) })
            val exception = Assertions.assertThrows(DelugeException::class.java) {
                runBlocking { session.request(request2) }
            }
            Assertions.assertAll(
                    Executable { Assertions.assertEquals(exType, exception.type) },
                    Executable { Assertions.assertEquals(exMsg, exception.message) },
                    Executable { Assertions.assertEquals(exBacktrace, exception.backtrace) }
            )
            Assertions.assertEquals(response3.data[2],
                    runBlocking { session.request(request3) })
        }
    }
}
