
package io.github.padconnect.receiver.native

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

val NATIVE_LIB_PATH = "${System.getenv("APPDATA")}\\PadConnectReceiver\\ViGEmClient.dll"

@Structure.FieldOrder(
    "wButtons",
    "bLeftTrigger",
    "bRightTrigger",
    "sThumbLX",
    "sThumbLY",
    "sThumbRX",
    "sThumbRY"
)
class XUSB_REPORT : Structure() {
    @JvmField var wButtons: Short = 0
    @JvmField var bLeftTrigger: Byte = 0
    @JvmField var bRightTrigger: Byte = 0
    @JvmField var sThumbLX: Short = 0
    @JvmField var sThumbLY: Short = 0
    @JvmField var sThumbRX: Short = 0
    @JvmField var sThumbRY: Short = 0
}

interface X360Notification: Callback {
    fun invoke(
        client: Pointer,
        target: Pointer,
        largeMotor: Byte,
        smallMotor: Byte,
        ledNumber: Byte,
        userData: Pointer?
    )
}

enum class VigemError(val code: Int) {
    NONE(0x20000000),

    BUS_NOT_FOUND(0xE0000001.toInt()),
    NO_FREE_SLOT(0xE0000002.toInt()),
    INVALID_TARGET(0xE0000003.toInt()),
    REMOVAL_FAILED(0xE0000004.toInt()),
    ALREADY_CONNECTED(0xE0000005.toInt()),
    TARGET_UNINITIALIZED(0xE0000006.toInt()),
    TARGET_NOT_PLUGGED_IN(0xE0000007.toInt()),
    BUS_VERSION_MISMATCH(0xE0000008.toInt()),
    BUS_ACCESS_FAILED(0xE0000009.toInt()),
    CALLBACK_ALREADY_REGISTERED(0xE0000010.toInt()),
    CALLBACK_NOT_FOUND(0xE0000011.toInt()),
    BUS_ALREADY_CONNECTED(0xE0000012.toInt()),
    BUS_INVALID_HANDLE(0xE0000013.toInt()),
    XUSB_USERINDEX_OUT_OF_RANGE(0xE0000014.toInt()),
    INVALID_PARAMETER(0xE0000015.toInt()),
    NOT_SUPPORTED(0xE0000016.toInt()),
    WINAPI(0xE0000017.toInt()),
    TIMED_OUT(0xE0000018.toInt()),
    IS_DISPOSING(0xE0000019.toInt()),

    UNKNOWN(-1);

    companion object {
        fun from(code: Int): VigemError {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }

    fun isSuccess(): Boolean = this == NONE
}

interface ViGEmClient : Library {
    fun vigem_alloc(): Pointer
    fun vigem_connect(client: Pointer): Int
    fun vigem_disconnect(client: Pointer)
    fun vigem_free(client: Pointer)

    fun vigem_target_x360_alloc(): Pointer
    fun vigem_target_add(client: Pointer, target: Pointer): Int
    fun vigem_target_remove(client: Pointer, target: Pointer)
    fun vigem_target_free(target: Pointer)

    fun vigem_target_x360_update(
        client: Pointer,
        target: Pointer,
        report: XUSB_REPORT
    ): Int

    fun vigem_target_x360_register_notification(
        client: Pointer,
        target: Pointer,
        callback: X360Notification,
        userData: Pointer?
    ): Int

    companion object {
        var INSTANCE: ViGEmClient = Native.load(NATIVE_LIB_PATH, ViGEmClient::class.java)
    }
}