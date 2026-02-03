
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadEvent
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.concurrent.LinkedBlockingQueue

class KBInputExecutor : InputExecutor {

    private val queue = LinkedBlockingQueue<GamepadEvent>()
    private val pressedKeys = mutableSetOf<Int>()

    private val robot = Robot().apply { autoDelay = 0 }

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

    private var x = 0f
    private var y = 0f

    private val thread = Thread {
        while (!Thread.interrupted()) {
            process(queue.take())
        }
    }.apply {
        name = "kb-input-executor"
        priority = Thread.MAX_PRIORITY
        start()
    }

    override fun submit(event: GamepadEvent) {
        queue.offer(event)
    }

    override fun shutdown() {
        thread.interrupt()
        pressedKeys.forEach { robot.keyRelease(it) }
    }

    private fun process(event: GamepadEvent) {
        when (event.type) {
            "button_down" ->
                keyMap[event.key]?.let { press(it) }

            "button_up" ->
                keyMap[event.key]?.let { release(it) }

            "axis" -> handleAxis(event)
        }
    }

    private fun handleAxis(event: GamepadEvent) {
        when (event.key) {
            "DPAD_X" -> x = clamp(event.value)
            "DPAD_Y" -> y = clamp(event.value)
        }

        set(KeyEvent.VK_A, x < 0)
        set(KeyEvent.VK_D, x > 0)
        set(KeyEvent.VK_W, y > 0)
        set(KeyEvent.VK_S, y < 0)
    }

    private fun set(key: Int, down: Boolean) {
        if (down) press(key) else release(key)
    }

    private fun press(key: Int) {
        if (pressedKeys.add(key)) robot.keyPress(key)
    }

    private fun release(key: Int) {
        if (pressedKeys.remove(key)) robot.keyRelease(key)
    }

    private fun clamp(v: Float) =
        when {
            v > 0.5f -> 1f
            v < -0.5f -> -1f
            else -> 0f
        }
}
