package net.ickis.deluge

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import net.ickis.deluge.event.DelugeEvent
import net.ickis.deluge.event.EmptyEvent
import net.ickis.deluge.event.getArg
import net.ickis.deluge.net.DelugeSocket
import net.ickis.deluge.net.RawRequest
import net.ickis.deluge.request.EventRequest
import net.ickis.deluge.request.Request
import net.ickis.deluge.request.SimpleRequest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.util.*

class DispatcherTest {
    class TestRequest<T> : SimpleRequest<T>("TestRequest") {
        override val args = emptyList<Any>()
    }

    @Test
    fun `Send success`() {
        val deferred = CompletableDeferred<Int>()
        val request = TestRequest<Int>()
        val id1 = 123
        val id2 = 124
        val socket = mockk<DelugeSocket>()
        val send1 = Send(request, deferred)
        val send2 = Send(request, deferred)
        val map: MutableMap<Int, Send<*>> = HashMap()

        coEvery { socket.write(RawRequest(request.serialize(id1))) } just Runs
        coEvery { socket.write(RawRequest(request.serialize(id2))) } just Runs

        runBlocking {
            send1.process(socket, id1, map)
            send2.process(socket, id2, map)
        }

        assertAll(
                { assertThat(map, aMapWithSize(2)) },
                { assertEquals(send1, map[id1]) },
                { assertEquals(send2, map[id2]) })
    }

    @Test
    fun `Subscribe success`() {
        class TestEvent(name: String) : EmptyEvent(name)

        fun testSubscription(name: String) = Subscribe(EventRequest(TestEvent(name)), Channel<Unit>())

        fun Map<String, List<Subscribe<*>>>.assertion(sub: Subscribe<*>, count: Int) = {
            val name = sub.request.event.name
            val list = getValue(name)
            assertThat(list, hasSize(count))
            list.forEach { assertEquals(name, it.request.event.name) }
        }

        val map = HashMap<String, List<Subscribe<*>>>()
        val sub1 = testSubscription("event1")
        val sub2 = testSubscription("event2")
        val sub3 = testSubscription("event3")
        val sub4 = testSubscription("event1")
        val sub5 = testSubscription("event2")
        val sub6 = testSubscription("event1")
        val subscriptions: List<Subscribe<Unit>> = listOf(sub1, sub2, sub3, sub4, sub5, sub6)
        subscriptions.forEach { it.process(map) }

        assertAll(
                { assertThat(map, aMapWithSize(3)) },
                map.assertion(sub1, 3),
                map.assertion(sub2, 2),
                map.assertion(sub3, 1)
        )
    }

    @Test
    fun `Receive Value success`() {
        val map = HashMap<Int, Send<*>>()
        val id1 = 1
        val id2 = 2
        val data1 = 456
        val data2 = "789"
        val deferred1 = CompletableDeferred<Int>()
        val deferred2 = CompletableDeferred<String>()
        val request1 = TestRequest<Int>()
        val request2 = TestRequest<String>()
        map[id1] = Send(request1, deferred1)
        map[id2] = Send(request2, deferred2)
        val value1 = DelugeResponse.Value(id1, data1)
        runBlocking { Receive(value1).process(map, Collections.emptyMap()) }
        assertAll(
                { runBlocking { assertEquals(data1, deferred1.await()) } },
                { assertThat(map, aMapWithSize(1)) }
        )
        val value2 = DelugeResponse.Value(id2, data2)
        runBlocking { Receive(value2).process(map, mutableMapOf()) }
        assertAll(
                { runBlocking { assertEquals(data2, deferred2.await()) } },
                { assertThat(map, anEmptyMap()) }
        )
    }

    @Test
    fun `Receive Value createResponse exception`() {
        val map = HashMap<Int, Send<*>>()
        val id = 1
        val exception = IllegalArgumentException("Don't know how to parse this value")
        val deferred = CompletableDeferred<Int>()
        val request = object : Request<Int>("ParseErrorRequest") {
            override val args = emptyList<Any>()
            override fun createResponse(rawValue: Any?) = throw exception
        }
        map[id] = Send(request, deferred)
        runBlocking { Receive(DelugeResponse.Value(id, "unparsable data")).process(map, mutableMapOf()) }
        assertAll(
                {
                    val actualEx = assertThrows<IllegalArgumentException> { runBlocking { deferred.await() } }
                    assertEquals(exception, actualEx.cause)
                },
                { assertThat(map, anEmptyMap()) }
        )
    }

    @Test
    fun `Receive Error success`() {
        val map = HashMap<Int, Send<*>>()
        val id = 1
        val exception = DelugeException(message = "Failure from daemon")
        val request = TestRequest<Int>()
        val deferred = CompletableDeferred<Int>()
        val error = DelugeResponse.Error(id, exception)
        map[id] = Send(request, deferred)
        runBlocking { Receive(error).process(map, mutableMapOf()) }
        assertAll(
                {
                    val actualEx = assertThrows<DelugeException> { runBlocking { deferred.await() } }
                    assertEquals(exception, actualEx)
                },
                { assertThat(map, anEmptyMap()) }
        )
    }

    @Test
    fun `Receive Event success`() {
        val eventName = "Event"
        data class TestNotification(val str: String, val int: Int)
        val delugeEvent = object : DelugeEvent<TestNotification>(eventName) {
            override fun createNotification(args: List<*>) = TestNotification(args.getArg(0), args.getArg(1))
        }
        val notification = TestNotification("123", 123)
        val map = HashMap<String, List<Subscribe<*>>>()
        val event = DelugeResponse.Event(eventName, listOf(notification.str, notification.int))
        val channel1 = Channel<TestNotification>(1)
        val channel2 = Channel<TestNotification>(1)
        map[eventName] = listOf(Subscribe(EventRequest(delugeEvent), channel1),
                Subscribe(EventRequest(delugeEvent), channel2))
        runBlocking { Receive(event).process(mutableMapOf(), map) }
        assertAll(
                { runBlocking { assertEquals(notification, channel1.receive()) } },
                { runBlocking { assertEquals(notification, channel2.receive()) } },
                { assertThat(map[eventName], hasSize(2)) }
        )
        channel2.close()
        runBlocking { Receive(event).process(mutableMapOf(), map) }
        assertAll(
                { runBlocking { assertEquals(notification, channel1.receive()) } },
                { assertThat(map[eventName], hasSize(1)) }
        )
    }

    @Test
    fun `Receive Event createNotification exception`() {
        val eventName = "Event"
        val exception = IllegalArgumentException("Don't know how to parse this value")
        val delugeEvent = object : DelugeEvent<Unit>(eventName) {
            override fun createNotification(args: List<*>) = throw exception
        }
        val map = HashMap<String, List<Subscribe<*>>>()
        val event = DelugeResponse.Event(eventName, emptyList<Any>())
        val channel = Channel<Unit>(1)
        map[eventName] = listOf(Subscribe(EventRequest(delugeEvent), channel))
        runBlocking { Receive(event).process(mutableMapOf(), map) }
        assertAll(
                {
                    val actualEx = assertThrows<IllegalArgumentException> { runBlocking { channel.receive() } }
                    assertEquals(exception, actualEx.cause)
                },
                { assertTrue(channel.isClosedForSend) },
                { assertThat(map[eventName], empty()) }
        )
    }
}
