package net.ickis.deluge.event

/**
 * Emitted when creating a torrent file remotely.
 */
object CreateTorrentProgressEvent : DelugeEvent<CreateTorrentProgress>("CreateTorrentProgressEvent") {
    override fun createNotification(args: List<*>) = CreateTorrentProgress(args.getArg(0), args.getArg(1))
}

data class CreateTorrentProgress(val pieceCount: Int, val numPieces: Int)
