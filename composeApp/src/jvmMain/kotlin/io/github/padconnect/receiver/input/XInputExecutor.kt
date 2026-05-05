
package io.github.padconnect.receiver.input

import com.sun.jna.Pointer
import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.dialogs.AlertDialogQueue
import io.github.padconnect.receiver.dialogs.AppDialog
import io.github.padconnect.receiver.native.ViGEmClient
import io.github.padconnect.receiver.native.VigemError
import io.github.padconnect.receiver.native.X360Notification
import io.github.padconnect.receiver.native.XUSB_REPORT

class XInputExecutor : InputExecutor {
    @Volatile
    private var latestState = GamepadState()

    private val client by lazy { ViGEmClient.INSTANCE.vigem_alloc() }
    private val target by lazy { ViGEmClient.INSTANCE.vigem_target_x360_alloc() }

    private val vibrationCallback = object : X360Notification {
        override fun invoke(
            client: Pointer,
            target: Pointer,
            largeMotor: Byte,
            smallMotor: Byte,
            ledNumber: Byte,
            userData: Pointer?
        ) {
            onRumble?.invoke(
                largeMotor.toInt() and 0xFF,
                smallMotor.toInt() and 0xFF
            )
        }
    }

    var onRumble: ((large: Int, small: Int) -> Unit)? = null

    private val report = XUSB_REPORT()

    private val thread = Thread {
        while (!Thread.interrupted()) {
            val s = latestState

            report.wButtons = s.buttons.toShort()
            report.sThumbLX = dz(s.lx)
            report.sThumbLY = dz(s.ly)
            report.sThumbRX = dz(s.rx)
            report.sThumbRY = dz(s.ry)
            report.bLeftTrigger = s.lt
            report.bRightTrigger = s.rt

            ViGEmClient.INSTANCE.vigem_target_x360_update(client, target, report)

            Thread.sleep(2) // 500Hz
        }
    }.apply {
        name = "xinput-executor"
        priority = Thread.MAX_PRIORITY
        start()
        println("Started $this")
    }

    init {
        ViGEmClient.INSTANCE.let {
            when (val result = VigemError.from(it.vigem_connect(client))) {
                VigemError.NONE -> {
                    // do nothing
                }
                VigemError.BUS_NOT_FOUND -> {
                    AlertDialogQueue.show(
                        AppDialog.Message(
                            title = "ViGEmBusDriver not found!",
                            message = "Please install ViGEmBusDriver first.",
                        )
                    )
                    return@let
                }
                else -> {
                    println("ViGEm error: $result")
                    return@let
                }
            }
            val addResult = VigemError.from(
                it.vigem_target_add(client, target)
            )

            if (!addResult.isSuccess()) {
                println("Target add failed: $addResult")
                return@let
            }

            val notifyResult = VigemError.from(
                it.vigem_target_x360_register_notification(
                    client,
                    target,
                    vibrationCallback,
                    null
                )
            )

            if (!notifyResult.isSuccess()) {
                println("Notification registration failed: $notifyResult")
            }
        }
    }

    override fun submit(state: GamepadState) {
        latestState = state
    }

    override fun shutdown() {
        ViGEmClient.INSTANCE.let {
            it.vigem_target_remove(client, target)
            it.vigem_target_free(target)
            it.vigem_disconnect(client)
            it.vigem_free(client)
        }
        thread.interrupt()
    }

    private fun dz(v: Short): Short {
        return if (kotlin.math.abs(v.toInt()) < 4000) 0 else v
    }
}
