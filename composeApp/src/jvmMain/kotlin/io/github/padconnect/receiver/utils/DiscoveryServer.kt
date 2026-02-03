package io.github.padconnect.receiver.utils

import java.net.DatagramPacket
import java.net.DatagramSocket

class DiscoveryServer(
    private val port: Int
) {

    private val socket = DatagramSocket(port)

    fun start() {
        Thread {
            println("Discovery server listening on 8083")

            val buffer = ByteArray(256)

            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val msg = String(packet.data, 0, packet.length)

                if (msg == "PADCONNECT_DISCOVER") {
                    val response = "PADCONNECT_HERE:$port".toByteArray()

                    val responsePacket = DatagramPacket(
                        response,
                        response.size,
                        packet.address,
                        packet.port
                    )

                    socket.send(responsePacket)
                    println("Responded to ${packet.address.hostAddress}")
                }
            }
        }.start()
    }
}
