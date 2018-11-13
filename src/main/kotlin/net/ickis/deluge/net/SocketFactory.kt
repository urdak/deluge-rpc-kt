package net.ickis.deluge.net

import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509ExtendedTrustManager

internal object SocketFactory {
    private const val ALGORITHM = "TLSv1.2"

    fun createSocket(address: String, port: Int): SSLSocket {
        val context = createSSLContext()
        val mySocket: SSLSocket = context.socketFactory.createSocket(address, port) as SSLSocket
        mySocket.enabledProtocols = arrayOf(ALGORITHM)
        return mySocket
    }

    private fun createSSLContext() = SSLContext.getInstance(ALGORITHM)
            .also { it.init(null, arrayOf(AnyTrustManager), SecureRandom()) }
}

internal object AnyTrustManager : X509ExtendedTrustManager() {
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?, p2: Socket?) {}
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?, p2: SSLEngine?) {}
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?, p2: Socket?) {}
    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?, p2: SSLEngine?) {}
    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
