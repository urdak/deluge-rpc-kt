package net.ickis.deluge.request

/**
 * Establishes an authenticated session.
 */
class LoginRequest(
        private val username: String,
        private val password: String
) : SimpleRequest<Int>("daemon.login") {
    override val args: List<Any>
        get() = listOf(username, password)
}
