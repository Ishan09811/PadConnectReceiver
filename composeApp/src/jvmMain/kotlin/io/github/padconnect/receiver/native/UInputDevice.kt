
package io.github.padconnect.receiver.native

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure

const val O_WRONLY = 1
const val O_NONBLOCK = 0x800
const val O_RDWR = 2

class UInputDevice {
    private var fd = -1

    fun create() {
        fd = CLib.INSTANCE.open("/dev/uinput", O_RDWR or O_NONBLOCK)
        if (fd < 0) {
            error("Failed to open /dev/uinput")
        }
        // Enable buttons
        ioctl(LinuxInput.UI_SET_EVBIT, LinuxInput.EV_KEY)
        ioctl(LinuxInput.UI_SET_EVBIT, LinuxInput.EV_ABS)

        // Xbox buttons
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_A)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_B)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_X)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_Y)

        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_TL)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_TR)

        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_SELECT)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_START)

        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_THUMBL)
        ioctl(LinuxInput.UI_SET_KEYBIT, LinuxInput.BTN_THUMBR)

        // Axes
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_X)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_Y)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_RX)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_RY)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_Z)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_RZ)

        // Dpad
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_HAT0X)
        ioctl(LinuxInput.UI_SET_ABSBIT, LinuxInput.ABS_HAT0Y)

        val dev = UInputUserDev()
        dev.setDeviceName("PadConnect Virtual Xbox Controller")
        dev.write()

        dev.absmin[LinuxInput.ABS_X] = -32768
        dev.absmax[LinuxInput.ABS_X] = 32767

        dev.absmin[LinuxInput.ABS_Y] = -32768
        dev.absmax[LinuxInput.ABS_Y] = 32767

        dev.absmin[LinuxInput.ABS_RX] = -32768
        dev.absmax[LinuxInput.ABS_RX] = 32767

        dev.absmin[LinuxInput.ABS_RY] = -32768
        dev.absmax[LinuxInput.ABS_RY] = 32767

        dev.absmin[LinuxInput.ABS_Z] = 0
        dev.absmax[LinuxInput.ABS_Z] = 255

        dev.absmin[LinuxInput.ABS_RZ] = 0
        dev.absmax[LinuxInput.ABS_RZ] = 255

        CLib.INSTANCE.write(
            fd,
            dev.getPointer().getByteArray(0, dev.size()),
            dev.size()
        )
        ioctl(LinuxInput.UI_DEV_CREATE, 0)
    }

    fun emitGamepad(
        buttons: Int,
        lx: Int,
        ly: Int,
        rx: Int,
        ry: Int,
        lt: Int,
        rt: Int
    ) {
        // Left stick
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_X, lx)
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_Y, -ly)

        // Right stick
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_RX, rx)
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_RY, -ry)

        // Triggers
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_Z, lt)
        emit(LinuxInput.EV_ABS, LinuxInput.ABS_RZ, rt)

        // Face buttons
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_A, if ((buttons and 0x1000) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_B, if ((buttons and 0x2000) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_X, if ((buttons and 0x4000) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_Y, if ((buttons and 0x8000) != 0) 1 else 0)

        // Shoulder buttons
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_TL, if ((buttons and 0x0100) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_TR, if ((buttons and 0x0200) != 0) 1 else 0)

        // Start / Select
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_START, if ((buttons and 0x0010) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_SELECT, if ((buttons and 0x0020) != 0) 1 else 0)

        // Stick clicks
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_THUMBL, if ((buttons and 0x0040) != 0) 1 else 0)
        emit(LinuxInput.EV_KEY, LinuxInput.BTN_THUMBR, if ((buttons and 0x0080) != 0) 1 else 0)

        emit(LinuxInput.EV_SYN, LinuxInput.SYN_REPORT, 0)
    }

    private fun emit(type: Int, code: Int, value: Int) {
        val event = InputEvent(type, code, value)
        event.write()
        val bytes = event.getPointer().getByteArray(0, event.size())
        CLib.INSTANCE.write(fd, bytes, bytes.size)
    }

    fun destroy() {
        ioctl(LinuxInput.UI_DEV_DESTROY, 0)
        CLib.INSTANCE.close(fd)
    }

    private fun ioctl(request: Long, value: Int) {
        CLib.INSTANCE.ioctl(fd, request, value)
    }
}

class UInputUserDev : Structure() {

    @JvmField
    var name = ByteArray(80)

    @JvmField
    var id_bustype: Short = 0x03

    @JvmField
    var id_vendor: Short = 0x045E.toShort()

    @JvmField
    var id_product: Short = 0x028E.toShort()

    @JvmField
    var id_version: Short = 1

    @JvmField
    var ff_effects_max: Int = 0

    @JvmField
    var absmax = IntArray(64)

    @JvmField
    var absmin = IntArray(64)

    @JvmField
    var absfuzz = IntArray(64)

    @JvmField
    var absflat = IntArray(64)

    fun setDeviceName(deviceName: String) {
        val bytes = deviceName.toByteArray()
        System.arraycopy(bytes, 0, name, 0, bytes.size.coerceAtMost(79))
    }

    override fun getFieldOrder() = listOf(
        "name",
        "id_bustype",
        "id_vendor",
        "id_product",
        "id_version",
        "ff_effects_max",
        "absmax",
        "absmin",
        "absfuzz",
        "absflat"
    )
}

object LinuxInput {

    // Event types
    const val EV_SYN = 0x00
    const val EV_KEY = 0x01
    const val EV_ABS = 0x03

    // Sync
    const val SYN_REPORT = 0

    // Xbox buttons
    const val BTN_A = 0x130
    const val BTN_B = 0x131
    const val BTN_X = 0x133
    const val BTN_Y = 0x134

    const val BTN_TL = 0x136
    const val BTN_TR = 0x137

    const val BTN_SELECT = 0x13a
    const val BTN_START = 0x13b

    const val BTN_THUMBL = 0x13d
    const val BTN_THUMBR = 0x13e

    // DPad
    const val BTN_DPAD_UP = 0x220
    const val BTN_DPAD_DOWN = 0x221
    const val BTN_DPAD_LEFT = 0x222
    const val BTN_DPAD_RIGHT = 0x223

    // Axes
    const val ABS_X = 0x00
    const val ABS_Y = 0x01

    const val ABS_Z = 0x02
    const val ABS_RX = 0x03
    const val ABS_RY = 0x04
    const val ABS_RZ = 0x05

    const val ABS_HAT0X = 0x10
    const val ABS_HAT0Y = 0x11

    const val UI_DEV_CREATE = 21761L
    const val UI_DEV_DESTROY = 21762L

    const val UI_SET_EVBIT = 1074025828L
    const val UI_SET_KEYBIT = 1074025829L
    const val UI_SET_ABSBIT = 1074025831L
}

class InputEvent(
    var type: Int = 0,
    var code: Int = 0,
    var value: Int = 0
) : Structure() {

    @JvmField
    var tv_sec: Long = 0

    @JvmField
    var tv_usec: Long = 0

    @JvmField
    var type_: Short = type.toShort()

    @JvmField
    var code_: Short = code.toShort()

    @JvmField
    var value_: Int = value

    override fun getFieldOrder() = listOf(
        "tv_sec",
        "tv_usec",
        "type_",
        "code_",
        "value_"
    )
}

interface CLib : Library {

    fun ioctl(fd: Int, request: Long, arg: Int): Int

    fun open(path: String, flags: Int): Int

    fun write(fd: Int, buffer: ByteArray, count: Int): Int

    fun close(fd: Int): Int

    companion object {
        val INSTANCE = Native.load("c", CLib::class.java)
    }
}
