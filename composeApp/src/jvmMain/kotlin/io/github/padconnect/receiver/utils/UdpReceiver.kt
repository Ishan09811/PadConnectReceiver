
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

    @Volatile
    private var senderAddress: InetAddress? = null
    @Volatile
    private var senderPort: Int? = null

    @Volatile
    private var isLatencyFeatureEnabled = false
    @Volatile
    private var isRumbleFeatureEnabled = false

    fun start() {
        Thread {
            val buffer = ByteArray(21)
            val packet = DatagramPacket(buffer, buffer.size)

            while (!socket.isClosed) {
                socket.receive(packet)

                val bb = ByteBuffer.wrap(packet.data, 0, packet.length)
                    .order(ByteOrder.LITTLE_ENDIAN)

                val type = bb.get().toInt()

                if (type == 0) {
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
                    if (senderAddress != packet.address || senderPort != packet.port) {
                        senderAddress = packet.address
                        senderPort = packet.port
                    }
                    if (isLatencyFeatureEnabled) senderAddress?.let { sendLatency(bb.long) }
                }
            }
        }.apply {
            name = "udp-io"
            priority = Thread.MAX_PRIORITY
            start()
            println("Started $this")
        }
    }

    fun onRumble(large: Int, small: Int) {
        if (senderAddress == null || !isRumbleFeatureEnabled) return
        val buf = ByteArray(3)
        buf[0] = 1 // type = rumble
        buf[1] = large.toByte()
        buf[2] = small.toByte()

        val packet = DatagramPacket(buf, buf.size, senderAddress, senderPort!!)
        socket.send(packet)
    }

    fun sendLatency(sentTime: Long) {
        val responseBuffer = ByteBuffer.allocate(17)
            .order(ByteOrder.LITTLE_ENDIAN)

        responseBuffer.put(2) // type = latency
        responseBuffer.putLong(sentTime)
        responseBuffer.putLong(System.nanoTime())

        val responsePacket = DatagramPacket(
            responseBuffer.array(),
            17,
            senderAddress,
            senderPort!!
        )

        socket.send(responsePacket)
    }

    fun setEnabledFeatures(features: Int) {
        isRumbleFeatureEnabled = (features and FEATURE_RUMBLE) != 0
        isLatencyFeatureEnabled = (features and FEATURE_LATENCY) != 0
    }

    fun stop() {
        socket.close()
    }
}
