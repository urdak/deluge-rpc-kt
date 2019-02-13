package net.ickis.deluge.samples;

import net.ickis.deluge.api.DelugeJavaClient;
import net.ickis.deluge.api.Torrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class SimpleClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String ip = "localhost";
        int port = 3333;
        String username = "username";
        String password = "password";
        String magnetLink = "";
        try (DelugeJavaClient client = new DelugeJavaClient(ip, port, username, password)) {
            CompletableFuture<String> torrentIdFuture = client.addTorrent(magnetLink);
            String torrentId = torrentIdFuture.get();
            CompletableFuture<Torrent> torrentStatus = client.getTorrentStatus(torrentId);
            client.removeTorrent(torrentId, true).join();
        }
    }
}
