package net.ickis.deluge.request

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

internal class TorrentPathRequest(
        private val path: Path
) : Request<String>("core.add_torrent_file") {
    override val args: List<Any>
        get() = listOf(
                path.fileName.toString(),
                Base64.getEncoder().encodeToString(Files.readAllBytes(path)),
                emptyMap<String, String>()
        )
}
