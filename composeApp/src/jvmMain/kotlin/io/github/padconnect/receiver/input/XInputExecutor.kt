
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.dialogs.AlertDialogQueue
import io.github.padconnect.receiver.dialogs.AppDialog
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.concurrent.thread

class XInputExecutor : InputExecutor {
    @Volatile
    private var latestState = GamepadState()

    var onRumble: ((large: Int, small: Int) -> Unit)? = null

    private val arena = Arena.ofShared()

    private val client: MemorySegment
    private val target: MemorySegment
    private val reportSegment: MemorySegment
    private val thread: Thread

    companion object {
        init {
            val dllResource = this::class.java.classLoader.getResource("ViGEmClient.dll") ?: throw Exception("Could not find ViGEmClient.dll")
            val path = if (dllResource.protocol == "file") {
                java.io.File(dllResource.toURI()).absolutePath
            } else {
                val tempFile = java.io.File.createTempFile("ViGEmClient", ".dll").apply { deleteOnExit() }
                dllResource.openStream().use { input -> tempFile.outputStream().use { input.copyTo(it) } }
                tempFile.absolutePath
            }
            System.load(path)
        }

        private val linker = Linker.nativeLinker()
        private val lookup = SymbolLookup.loaderLookup()

        private val XUSB_REPORT_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_SHORT.withName("wButtons"),
            ValueLayout.JAVA_BYTE.withName("bLeftTrigger"),
            ValueLayout.JAVA_BYTE.withName("bRightTrigger"),
            ValueLayout.JAVA_SHORT.withName("sThumbLX"),
            ValueLayout.JAVA_SHORT.withName("sThumbLY"),
            ValueLayout.JAVA_SHORT.withName("sThumbRX"),
            ValueLayout.JAVA_SHORT.withName("sThumbRY")
        )

        private val vigem_alloc = linker.downcallHandle(lookup.find("vigem_alloc").get(), FunctionDescriptor.of(ValueLayout.ADDRESS))
        private val vigem_connect = linker.downcallHandle(lookup.find("vigem_connect").get(), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS))
        private val vigem_disconnect = linker.downcallHandle(lookup.find("vigem_disconnect").get(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))
        private val vigem_free = linker.downcallHandle(lookup.find("vigem_free").get(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))

        private val vigem_target_x360_alloc = linker.downcallHandle(lookup.find("vigem_target_x360_alloc").get(), FunctionDescriptor.of(ValueLayout.ADDRESS))
        private val vigem_target_add = linker.downcallHandle(lookup.find("vigem_target_add").get(), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
        private val vigem_target_remove = linker.downcallHandle(lookup.find("vigem_target_remove").get(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
        private val vigem_target_free = linker.downcallHandle(lookup.find("vigem_target_free").get(), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))

        private val vigem_target_x360_update = linker.downcallHandle(
            lookup.find("vigem_target_x360_update").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, XUSB_REPORT_LAYOUT)
        )

        private val vigem_target_x360_register_notification = linker.downcallHandle(
            lookup.find("vigem_target_x360_register_notification").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
        )

        private val wButtonsVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wButtons"))
        private val bLeftTriggerVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bLeftTrigger"))
        private val bRightTriggerVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bRightTrigger"))
        private val sThumbLXVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("sThumbLX"))
        private val sThumbLYVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("sThumbLY"))
        private val sThumbRXVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("sThumbRX"))
        private val sThumbRYVar = XUSB_REPORT_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("sThumbRY"))
    }

    @Suppress("unused")
    private fun onVibrationNative(client: MemorySegment, target: MemorySegment, largeMotor: Byte, smallMotor: Byte, ledNumber: Byte, userData: MemorySegment) {
        onRumble?.invoke(
            largeMotor.toInt() and 0xFF,
            smallMotor.toInt() and 0xFF
        )
    }

    init {
        client = vigem_alloc.invokeExact() as MemorySegment
        target = vigem_target_x360_alloc.invokeExact() as MemorySegment
        reportSegment = arena.allocate(XUSB_REPORT_LAYOUT)

        val connectRes = vigem_connect.invokeExact(client) as Int
        when (val result = VigemError.from(connectRes)) {
            VigemError.NONE -> {}
            VigemError.BUS_NOT_FOUND -> {
                AlertDialogQueue.show(
                    AppDialog.Message(
                        title = "ViGEmBusDriver not found!",
                        message = "Please install ViGEmBusDriver first.",
                    )
                )
            }
            else -> {
                println("ViGEm error: $result")
            }
        }

        val addResult = VigemError.from(vigem_target_add.invokeExact(client, target) as Int)
        if (!addResult.isSuccess()) {
            println("Target add failed: $addResult")
        }

        val callbackHandle = MethodHandles.lookup().findVirtual(
            XInputExecutor::class.java,
            "onVibrationNative",
            MethodType.methodType(
                Void.TYPE,
                MemorySegment::class.java,
                MemorySegment::class.java,
                Byte::class.javaPrimitiveType,
                Byte::class.javaPrimitiveType,
                Byte::class.javaPrimitiveType,
                MemorySegment::class.java
            )
        ).bindTo(this)

        val callbackStub = linker.upcallStub(
            callbackHandle,
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.ADDRESS),
            arena
        )

        val notifyResult = VigemError.from(
            vigem_target_x360_register_notification.invokeExact(client, target, callbackStub, MemorySegment.NULL) as Int
        )
        if (!notifyResult.isSuccess()) {
            println("Notification registration failed: $notifyResult")
        }

        thread = thread(start = false, name = "xinput-executor", priority = Thread.MAX_PRIORITY) {
            while (!Thread.interrupted()) {
                val s = latestState

                wButtonsVar.set(reportSegment, 0L, s.buttons.toShort())
                bLeftTriggerVar.set(reportSegment, 0L, s.lt)
                bRightTriggerVar.set(reportSegment, 0L, s.rt)
                sThumbLXVar.set(reportSegment, 0L, dz(s.lx))
                sThumbLYVar.set(reportSegment, 0L, dz(s.ly))
                sThumbRXVar.set(reportSegment, 0L, dz(s.rx))
                sThumbRYVar.set(reportSegment, 0L, dz(s.ry))

                vigem_target_x360_update.invokeExact(client, target, reportSegment) as Int

                try {
                    Thread.sleep(2)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply { start() }
    }

    override fun submit(state: GamepadState) {
        latestState = state
    }

    override fun shutdown() {
        thread.interrupt()
        try {
            vigem_target_remove.invokeExact(client, target)
            vigem_target_free.invokeExact(target)
            vigem_disconnect.invokeExact(client)
            vigem_free.invokeExact(client)
        } catch (e: Throwable) {
            println("Error during native cleanup: ${e.message}")
        }
        arena.close()
    }

    private fun dz(v: Short): Short {
        return if (kotlin.math.abs(v.toInt()) < 4000) 0 else v
    }
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