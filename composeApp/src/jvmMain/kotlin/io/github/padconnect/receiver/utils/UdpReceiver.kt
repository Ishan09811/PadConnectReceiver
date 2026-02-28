
package io.github.padconnect.receiver.utils

import io.github.padconnect.receiver.data.GamepadState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UdpReceiver(
    port: Int,
    private val onEvent: (GamepadState) -> Unit
) {
    private val socket = DatagramSocket(null).apply {
        reuseAddress = true
        bind(InetSocketAddress(port))
    }

    fun start(sendLatencyStats: Boolean = true) {
        Thread {
            val buffer = ByteArray(20)
            val packet = DatagramPacket(buffer, buffer.size)

            while (!socket.isClosed) {
                socket.receive(packet)

                val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                    .order(ByteOrder.LITTLE_ENDIAN)

                val state = GamepadState(
                    buttons = bb.short.toInt(),
                    lx = bb.short,
                    ly = bb.short,
                    rx = bb.short,
                    ry = bb.short,
                    lt = bb.get(),
                    rt = bb.get()
                )
                onEvent(state)
                if (sendLatencyStats) sendLatency(bb.long, packet.address, packet.port)
            }
        }.apply {
            name = "udp-io"
            priority = Thread.MAX_PRIORITY
            start()
            println("Started $this")
        }
    }

    fun sendLatency(sentTime: Long, address: InetAddress, port: Int) {
        val responseBuffer = ByteBuffer.allocate(16)
            .order(ByteOrder.LITTLE_ENDIAN)

        responseBuffer.putLong(sentTime)
        responseBuffer.putLong(System.nanoTime())

        val responsePacket = DatagramPacket(
            responseBuffer.array(),
            16,
            address,
            port
        )

        socket.send(responsePacket)
    }

    fun stop() {
        socket.close()
    }
}
