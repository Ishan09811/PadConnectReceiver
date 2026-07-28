
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.native.UInputDevice
import kotlin.concurrent.thread

class LinuxInputExecutor : InputExecutor {
    private val latestState = GamepadState()

    private val uinput = UInputDevice()

    init {
        uinput.create()
    }

    private val updateThread = thread(
        name = "linux-gamepad",
        priority = Thread.MAX_PRIORITY
    ) {
        while (!Thread.interrupted()) {
            val s = latestState

            uinput.emitGamepad(
                buttons = s.buttons,
                lx = s.lx.toInt(),
                ly = s.ly.toInt(),
                rx = s.rx.toInt(),
                ry = s.ry.toInt(),
                lt = s.lt.toInt() and 0xFF,
                rt = s.rt.toInt() and 0xFF
            )

            Thread.sleep(2)
        }
    }

    override fun submit(buttons: Int, lx: Short, ly: Short, rx: Short, ry: Short, lt: Byte, rt: Byte) {
        latestState.apply {
            this.buttons = buttons
            this.lx = lx
            this.ly = ly
            this.rx = rx
            this.ry = ry
            this.lt = lt
            this.rt = rt
        }
    }

    override fun shutdown() {
        updateThread.interrupt()
        uinput.destroy()
    }
}