package net.ickis.deluge.samples;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import net.ickis.deluge.event.TorrentRemovedEvent;

public class HandlingEvents {
    public static void main(String[] args) {
        SimpleClient.create(client -> {
            Observable<String> observable = client.subscribe(TorrentRemovedEvent.INSTANCE);
            Disposable disposable = observable.subscribe(torrentId -> System.out.println("Removed " + torrentId));
            sleep(10000);
            disposable.dispose();
            return null;
        });
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {}
    }
}
