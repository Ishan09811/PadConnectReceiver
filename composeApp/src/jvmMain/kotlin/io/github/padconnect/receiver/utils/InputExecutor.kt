
package io.github.padconnect.receiver.utils

import io.github.padconnect.receiver.data.GamepadEvent
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.concurrent.LinkedBlockingQueue

class InputExecutor {
    private val queue = LinkedBlockingQueue<GamepadEvent>()

    private val robot = Robot().apply {
        autoDelay = 0
    }

    private val keyMap = mapOf(
        "A" to KeyEvent.VK_SPACE,
        "B" to KeyEvent.VK_CONTROL,
        "X" to KeyEvent.VK_X,
        "Y" to KeyEvent.VK_Y,
        "LB" to KeyEvent.VK_Q,
        "RB" to KeyEvent.VK_E,
        "START" to KeyEvent.VK_ENTER,
        "SELECT" to KeyEvent.VK_SHIFT
    )

    private var lastX = 0f
    private var lastY = 0f

    init {
        Thread {
            while (true) {
                val event = queue.take()
                process(event)
            }
        }.apply {
            name = "input-executor"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun submit(event: GamepadEvent) {
        queue.offer(event)
    }

    private fun process(event: GamepadEvent) {
        when (event.type) {
            "button_down" -> {
                keyMap[event.key]?.let { keyCode ->
                    robot.keyPress(keyCode)
                }
            }

            "button_up" -> {
                keyMap[event.key]?.let { keyCode ->
                    robot.keyRelease(keyCode)
                }
            }

            "axis" -> {
                handleAxis(event)
            }
        }
    }

    private fun handleAxis(event: GamepadEvent) {
        when (event.key) {
            "DPAD_X" -> {
                updateAxis(
                    old = lastX,
                    new = event.value ?: 0f,
                    negativeKey = KeyEvent.VK_A,
                    positiveKey = KeyEvent.VK_D
                )
                lastX = event.value ?: 0f
            }

            "DPAD_Y" -> {
                updateAxis(
                    old = lastY,
                    new = event.value ?: 0f,
                    negativeKey = KeyEvent.VK_W,
                    positiveKey = KeyEvent.VK_S
                )
                lastY = event.value ?: 0f
            }
        }
    }

    private fun updateAxis(
        old: Float,
        new: Float,
        negativeKey: Int,
        positiveKey: Int
    ) {
        if (old < 0) robot.keyRelease(negativeKey)
        if (old > 0) robot.keyRelease(positiveKey)

        if (new < 0) robot.keyPress(negativeKey)
        if (new > 0) robot.keyPress(positiveKey)
    }
}
