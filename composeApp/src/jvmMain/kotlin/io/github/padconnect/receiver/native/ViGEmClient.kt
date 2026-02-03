
package io.github.padconnect.receiver.native

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

var retries = 1

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

    companion object {
        var INSTANCE: ViGEmClient? = null
        init {
            INSTANCE = loadLibrary()
        }
    }
}

fun loadLibrary(): ViGEmClient? {
    return try {
        Native.load(
            "ViGEmClient",
            ViGEmClient::class.java
        )
    } catch (e: Exception) {
       if (retries < 4) {
           retries += 1
           e.printStackTrace()
           loadLibrary()
       } else { null }
    }
}
