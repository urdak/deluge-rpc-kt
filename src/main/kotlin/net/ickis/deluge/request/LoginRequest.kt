package net.ickis.deluge.request

internal class LoginRequest(
        private val username: String,
        private val password: String
) : Request<Int>("daemon.login") {
    override val args: List<Any>
        get() = listOf(username, password)
}
