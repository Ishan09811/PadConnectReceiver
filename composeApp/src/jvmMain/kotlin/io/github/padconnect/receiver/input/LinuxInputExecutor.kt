
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.native.UInputDevice
import kotlin.concurrent.thread

class LinuxInputExecutor : InputExecutor {
    private val uinput = UInputDevice()

    init {
        uinput.create()
    }

    override fun submit(buttons: Int, lx: Short, ly: Short, rx: Short, ry: Short, lt: Byte, rt: Byte) {
        uinput.emitGamepad(
            buttons = buttons,
            lx = lx.toInt(),
            ly = ly.toInt(),
            rx = rx.toInt(),
            ry = ry.toInt(),
            lt = lt.toInt() and 0xFF,
            rt = rt.toInt() and 0xFF
        )
    }

    override fun shutdown() {
        uinput.destroy()
    }
}