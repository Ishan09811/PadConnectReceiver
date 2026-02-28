package io.github.padconnect.receiver.utils

import java.net.DatagramPacket
import java.net.DatagramSocket

class DiscoveryServer(
    private val port: Int
) {

    private val socket = DatagramSocket(port)

    fun start() {
        Thread {
            println("Discovery server listening on $port")

            val buffer = ByteArray(256)

            // TODO: stop once responded to free up threads
            while (true) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val msg = String(packet.data, 0, packet.length)

                if (msg == "PADCONNECT_DISCOVER") {
                    val response = "PADCONNECT_HERE:8082".toByteArray()

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
        }.apply {
            name = "discovery-server"
            start()
            println("Started $this")
        }
    }
}
