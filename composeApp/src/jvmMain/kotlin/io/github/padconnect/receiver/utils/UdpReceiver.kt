
package io.github.padconnect.receiver.utils

import io.github.padconnect.receiver.data.GamepadEvent
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlinx.serialization.json.Json

class UdpReceiver(
    port: Int,
    private val onEvent: (GamepadEvent) -> Unit
) {
    private val socket = DatagramSocket(null).apply {
        reuseAddress = true
        bind(InetSocketAddress(port))
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun start() {
        Thread {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            while (!socket.isClosed) {
                socket.receive(packet)

                val msg = String(packet.data, 0, packet.length)
                val event = json.decodeFromString<GamepadEvent>(msg)

                onEvent(event)
            }
        }.apply {
            name = "udp-io"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        socket.close()
    }
}
