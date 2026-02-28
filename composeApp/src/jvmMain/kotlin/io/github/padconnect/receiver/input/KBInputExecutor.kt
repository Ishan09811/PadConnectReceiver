
package io.github.padconnect.receiver.input

/*import io.github.padconnect.receiver.data.EventType
import io.github.padconnect.receiver.data.GamepadEvent
import io.github.padconnect.receiver.data.GamepadKey
import java.awt.Robot
import java.awt.event.KeyEvent
import java.util.concurrent.LinkedBlockingQueue

class KBInputExecutor : InputExecutor {

    private val queue = LinkedBlockingQueue<GamepadEvent>()
    private val pressedKeys = mutableSetOf<Int>()

    private val robot = Robot().apply { autoDelay = 0 }

    private val keyMap = mapOf(
        GamepadKey.A to KeyEvent.VK_SPACE,
        GamepadKey.B to KeyEvent.VK_CONTROL,
        GamepadKey.X to KeyEvent.VK_X,
        GamepadKey.Y to KeyEvent.VK_Y,
        GamepadKey.LB to KeyEvent.VK_Q,
        GamepadKey.RB to KeyEvent.VK_E,
        GamepadKey.START to KeyEvent.VK_ENTER,
        GamepadKey.SELECT to KeyEvent.VK_SHIFT
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
            EventType.BUTTON_DOWN ->
                keyMap[event.key]?.let { press(it) }

            EventType.BUTTON_UP ->
                keyMap[event.key]?.let { release(it) }

            EventType.AXIS -> handleAxis(event)
        }
    }

    private fun handleAxis(event: GamepadEvent) {
        when (event.key) {
            GamepadKey.L_ANALOG_STICK -> {
                x = clamp(event.value.first)
                y = clamp(event.value.second)
            }
            else -> {}
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
}*/
