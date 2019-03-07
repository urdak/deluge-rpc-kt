package net.ickis.deluge.samples

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ickis.deluge.event.TorrentRemovedEvent

fun main() {
    simpleClient { client ->
        val channel = client.subscribe(TorrentRemovedEvent)
        GlobalScope.launch {
            for (torrentId in channel) {
                println("Removed $torrentId")
            }
        }
        delay(10000)
        channel.cancel() // clean up when channel is no longer needed
    }
}
