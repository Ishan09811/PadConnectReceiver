
package io.github.padconnect.receiver.input

import com.sun.jna.Pointer
import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.native.ViGEmClient
import io.github.padconnect.receiver.native.XUSB_REPORT

class XInputExecutor : InputExecutor {
    @Volatile
    private var latestState = GamepadState()

    private var client: Pointer? = ViGEmClient.INSTANCE?.vigem_alloc()
    private var target: Pointer? = ViGEmClient.INSTANCE?.vigem_target_x360_alloc()
    private val report = XUSB_REPORT()

    private val thread = Thread {
        while (!Thread.interrupted()) {
            initIfNotAlready()
            val s = latestState

            report.wButtons = s.buttons.toShort()
            report.sThumbLX = dz(s.lx)
            report.sThumbLY = dz(s.ly)
            report.sThumbRX = dz(s.rx)
            report.sThumbRY = dz(s.ry)
            report.bLeftTrigger = s.lt
            report.bRightTrigger = s.rt

            ViGEmClient.INSTANCE!!
                .vigem_target_x360_update(client!!, target!!, report)

            Thread.sleep(2) // 500Hz
        }
    }.apply {
        name = "xinput-executor"
        priority = Thread.MAX_PRIORITY
        start()
        println("Started $this")
    }

    init {
        ViGEmClient.INSTANCE?.let {
            it.vigem_connect(client!!)
            it.vigem_target_add(client!!, target!!)
        }
    }

    override fun submit(state: GamepadState) {
        latestState = state
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

    private fun dz(v: Short): Short {
        return if (kotlin.math.abs(v.toInt()) < 4000) 0 else v
    }

    private fun initIfNotAlready() {
        if (ViGEmClient.INSTANCE != null && client != null && target != null) {
            return
        } else {
            ViGEmClient.INSTANCE?.let {
                client = it.vigem_alloc()
                target = it.vigem_target_x360_alloc()
                it.vigem_connect(client!!)
                it.vigem_target_add(client!!, target!!)
            }
        }
    }
}
