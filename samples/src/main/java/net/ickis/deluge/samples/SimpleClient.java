package net.ickis.deluge.samples;

import net.ickis.deluge.api.DelugeJavaClient;
import net.ickis.deluge.api.Torrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SimpleClient {
    public static final String IP = "localhost";
    public static final int PORT = 3333;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MAGNET_LINK = "";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (DelugeJavaClient client = new DelugeJavaClient(IP, PORT, USERNAME, PASSWORD)) {
            CompletableFuture<String> torrentIdFuture = client.addTorrent(MAGNET_LINK);
            String torrentId = torrentIdFuture.get();
            if (torrentId == null) throw new IllegalArgumentException("Bad magnet link");
            CompletableFuture<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
            client.removeTorrent(torrentId, true).join();
        }
    }

    public static <T> T create(Function<DelugeJavaClient, T> function) {
        try (DelugeJavaClient client = new DelugeJavaClient(IP, PORT, USERNAME, PASSWORD)) {
            return function.apply(client);
        }
    }
}
