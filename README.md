## Deluge-RPC-Kt
Provides an extendable API for a Java/Kotlin based on [Deluge RPC](https://deluge.readthedocs.io/en/develop/reference/rpc.html).

### Using the client

##### Kotlin
Using [coroutines](https://github.com/Kotlin/kotlinx.coroutines):
```kotlin
runBlocking {
    delugeClient(IP, PORT, USERNAME, PASSWORD) { client ->
        val torrentId: String = client.addTorrent(MAGNET_LINK) ?: throw IllegalArgumentException("Bad magnet link")
        val torrent: Torrent = client.getTorrentStatus(torrentId)
        val removed: Boolean? = client.removeTorrent(torrentId, true)
    }
}
```
> You can get full code [here](samples/src/main/kotlin/net/ickis/deluge/samples/SimpleClient.kt)
##### Java
Using [RxJava Single](http://reactivex.io/documentation/single.html):
```java
try (DelugeJavaClient client = new DelugeJavaClient(IP, PORT, USERNAME, PASSWORD)) {
    Single<Optional<String>> torrentIdSingle = client.addTorrent(MAGNET_LINK);
    String torrentId = torrentIdSingle.blockingGet().orElseThrow(() -> new IllegalArgumentException("Bad magnet link"));
    Single<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
    client.removeTorrent(torrentId, true).blockingGet();
}
```
> You can get full code [here](samples/src/main/java/net/ickis/deluge/samples/SimpleClient.java)

### Subscribing to Deluge Events
Deluge RPC provides a way to get notifications about events that occur on the daemon.
##### Kotlin
Using [ReceiveChannel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-receive-channel/):
```kotlin
val channel = client.subscribe(TorrentRemovedEvent)
GlobalScope.launch {
    for (torrentId in channel) {
        println("Removed $torrentId")
    }
}
delay(10000)
channel.cancel() // clean up when channel is no longer needed
```
> You can get full code [here](samples/src/main/kotlin/net/ickis/deluge/samples/HandlingEvents.kt)
##### Java
Using [RxJava Observable](http://reactivex.io/documentation/observable.html)
```java
Observable<String> observable = client.subscribe(TorrentRemovedEvent.INSTANCE);
Disposable disposable = observable.subscribe(torrentId -> System.out.println("Removed " + torrentId));
sleep(10000);
disposable.dispose();
```
> You can get full code [here](samples/src/main/java/net/ickis/deluge/samples/HandlingEvents.java)

### Extending the client

Additional commands can be added to the client by extending the [Request](src/main/kotlin/net/ickis/deluge/request/Request.kt) class.
These commands need to be compatible with the [Deluge RPC API](https://deluge.readthedocs.io/en/develop/reference/api.html).

```kotlin
class CustomCommand(param: Int) : SimpleRequest<Int>("custom_command") {
    override val args: List<Any> = listOf(param)
}

suspend fun DelugeClient.customCommand(param: Int) = request(CustomCommand(param))
```
> You can get full code [here](samples/src/main/kotlin/net/ickis/deluge/samples/ExtendingTheClient.kt)
### Gradle dependency
TBD
