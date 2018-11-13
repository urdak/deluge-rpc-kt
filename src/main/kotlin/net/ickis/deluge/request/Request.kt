package net.ickis.deluge.request

abstract class Request<T>(private val method: String) {
    protected abstract val args: List<Any>
    protected open val kwargs: Map<Any, Any> = emptyMap()

    internal fun serialize(id: Int): List<List<Any>> {
        return listOf(listOf(id, method, args, kwargs))
    }
}
