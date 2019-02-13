## Deluge-RPC-Kt
Provides an extendable API for a Java/Kotlin based on [Deluge RPC](https://deluge.readthedocs.io/en/develop/reference/rpc.html).

### Using the client

##### Kotlin
Using coroutines:
```kotlin
runBlocking {
    DelugeClient(ip, port, username, password).use { client ->
        val torrentId: String = client.addTorrent(/*magnetLink/URL/Path*/)
        val torrent: Torrent = client.getTorrentStatus(torrentId)
        val removed: Boolean? = client.removeTorrent(torrentId, true)
    }
}
```

##### Java
Using java.util.concurrent.CompletableFuture:
```java
try (DelugeJavaClient client = new DelugeJavaClient(ip, port, username, password)) {
    CompletableFuture<String> torrentIdFuture = client.addTorrent(/*magnetLink/URL/Path*/);
    String torrentId = torrentIdFuture.get();
    CompletableFuture<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
    client.removeTorrent(torrentId, true).join();
}
```

### Extending the client

Additional commands can be added to the client by extending the [Request](src/main/kotlin/net/ickis/deluge/request/Request.kt) class.
These commands need to be compatible with the [Deluge RPC API](https://deluge.readthedocs.io/en/develop/reference/api.html).

```kotlin
object FreeSpace : Request<Int>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}

val request: Int = client.request(FreeSpace)
```

### Gradle dependency
TBD