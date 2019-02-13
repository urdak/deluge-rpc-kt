## Deluge-RPC-Kt
Provides an extendable API for a Java/Kotlin based on [Deluge RPC](https://deluge.readthedocs.io/en/develop/reference/rpc.html).

### Using the client

##### Kotlin
Using [coroutines](https://github.com/Kotlin/kotlinx.coroutines):
```kotlin
runBlocking {
    DelugeClient(ip, port, username, password).use { client ->
        val torrentId: String = client.addTorrent(magnetLink)
        val torrent: Torrent = client.getTorrentStatus(torrentId)
        val removed: Boolean? = client.removeTorrent(torrentId, true)
    }
}
```
> You can get full code [here](samples/src/main/kotlin/net/ickis/deluge/samples/SimpleClient.kt)
##### Java
Using [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html):
```java
try (DelugeJavaClient client = new DelugeJavaClient(ip, port, username, password)) {
    CompletableFuture<String> torrentIdFuture = client.addTorrent(magnetLink);
    String torrentId = torrentIdFuture.get();
    CompletableFuture<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
    client.removeTorrent(torrentId, true).join();
}
```
> You can get full code [here](samples/src/main/java/net/ickis/deluge/samples/SimpleClient.java)
### Extending the client

Additional commands can be added to the client by extending the [Request](src/main/kotlin/net/ickis/deluge/request/Request.kt) class.
These commands need to be compatible with the [Deluge RPC API](https://deluge.readthedocs.io/en/develop/reference/api.html).

```kotlin
object FreeSpace : Request<Int>("core.get_free_space") {
    override val args: List<Any> = emptyList()
}

suspend fun DelugeClient.freeSpace() = request(FreeSpace)
```
> You can get full code [here](samples/src/main/kotlin/net/ickis/deluge/samples/ExtendingTheClient.kt)
### Gradle dependency
TBD
