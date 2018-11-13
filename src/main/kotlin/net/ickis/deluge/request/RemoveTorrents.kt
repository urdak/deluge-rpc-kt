package net.ickis.deluge.request

internal class RemoveTorrents(
        private val torrentId: String,
        private val removeData: Boolean
) : Request<Boolean?>("core.remove_torrent") {
    override val args: List<Any>
        get() = listOf(torrentId, removeData)
}
