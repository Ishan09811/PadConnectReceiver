
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadEvent
import io.github.padconnect.receiver.native.ViGEmClient
import io.github.padconnect.receiver.native.XUSB_REPORT
import com.sun.jna.Pointer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.roundToInt

class XInputExecutor : InputExecutor {

    private val queue = LinkedBlockingQueue<GamepadEvent>()

    private var client: Pointer? = ViGEmClient.INSTANCE?.vigem_alloc()
    private var target: Pointer? = ViGEmClient.INSTANCE?.vigem_target_x360_alloc()
    private val report = XUSB_REPORT()

    private val thread = Thread {
        while (!Thread.interrupted()) {
            process(queue.take())
            flush()
        }
    }.apply {
        name = "xinput-executor"
        priority = Thread.MAX_PRIORITY
        start()
    }

    init {
        ViGEmClient.INSTANCE?.let {
            it.vigem_connect(client!!)
            it.vigem_target_add(client!!, target!!)
        }
    }

    override fun submit(event: GamepadEvent) {
        queue.offer(event)
    }

    override fun shutdown() {
        ViGEmClient.INSTANCE?.let {
            it.vigem_target_remove(client!!, target!!)
            it.vigem_target_free(target!!)
            it.vigem_disconnect(client!!)
            it.vigem_free(client!!)
        }
        thread.interrupt()
    }

    private fun process(e: GamepadEvent) {
        when (e.type) {
            "button_down" -> setButton(e.key, true)
            "button_up" -> setButton(e.key, false)
            "axis" -> setAxis(e)
        }
    }

    private fun setButton(key: String?, down: Boolean) {
        val mask = when (key) {
            "A" -> 0x1000
            "B" -> 0x2000
            "X" -> 0x4000
            "Y" -> 0x8000
            "LB" -> 0x0100
            "RB" -> 0x0200
            "START" -> 0x0010
            "SELECT" -> 0x0020
            else -> return
        }

        report.wButtons =
            if (down) (report.wButtons.toInt() or mask).toShort()
            else (report.wButtons.toInt() and mask.inv()).toShort()
    }

    private fun setAxis(e: GamepadEvent) {
        val v = ((e.value) * Short.MAX_VALUE).roundToInt().toShort()

        when (e.key) {
            "DPAD_X" -> report.sThumbLX = v
            "DPAD_Y" -> report.sThumbLY = v
        }
    }

    private fun flush() {
        if (ViGEmClient.INSTANCE != null && client != null && target != null) {
            ViGEmClient.INSTANCE!!.vigem_target_x360_update(client!!, target!!, report)
        } else {
            ViGEmClient.INSTANCE?.let {
                client = it.vigem_alloc()
                target = it.vigem_target_x360_alloc()
                it.vigem_connect(client!!)
                it.vigem_target_add(client!!, target!!)
                it.vigem_target_x360_update(client!!, target!!, report)
            }
        }
    }
}
