package net.ickis.deluge.request

/**
 * Base class for requests that can be sent to the daemon using the [net.ickis.deluge.api.DelugeClient].
 * @param method The method that is called on the Deluge daemon.
 * @see <a href="https://deluge.readthedocs.io/en/develop/reference/api.html">Deluge RPC API</a>.
 */
abstract class Request<T>(private val method: String) {
    /**
     * Args of the request provided to the daemon.
     */
    protected abstract val args: List<Any>
    /**
     * Keyword arguments of the request provided to the daemon.
     */
    protected open val kwargs: Map<Any, Any> = emptyMap()

    /**
     * Serializes the request to a format that is supported by the Deluge RPC.
     */
    internal fun serialize(id: Int): List<List<Any>> {
        return listOf(listOf(id, method, args, kwargs))
    }
}
