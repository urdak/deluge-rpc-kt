package net.ickis.deluge.samples;

import io.reactivex.Single;
import net.ickis.deluge.api.DelugeJavaClient;
import net.ickis.deluge.api.Torrent;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SimpleClient {
    public static final String IP = "localhost";
    public static final int PORT = 3333;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MAGNET_LINK = "";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        try (DelugeJavaClient client = new DelugeJavaClient(IP, PORT, USERNAME, PASSWORD)) {
            Single<Optional<String>> torrentIdSingle = client.addTorrent(MAGNET_LINK);
            String torrentId = torrentIdSingle.blockingGet().orElseThrow(() -> new IllegalArgumentException("Bad magnet link"));
            Single<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
            client.removeTorrent(torrentId, true).blockingGet();
        }
    }

    static <T> T create(Function<DelugeJavaClient, T> function) {
        try (DelugeJavaClient client = new DelugeJavaClient(IP, PORT, USERNAME, PASSWORD)) {
            return function.apply(client);
        }
    }
}
