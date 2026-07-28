
package io.github.padconnect.receiver.viewmodel

import androidx.lifecycle.ViewModel
import io.github.padconnect.receiver.SystemInfo
import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.input.LinuxInputExecutor
import io.github.padconnect.receiver.input.XInputExecutor
import io.github.padconnect.receiver.utils.DiscoveryServer
import io.github.padconnect.receiver.utils.UdpReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReceiverViewModel : ViewModel() {
    private val _lastState = MutableStateFlow<GamepadState?>(null)
    val lastState: StateFlow<GamepadState?> = _lastState

    var lastUiUpdate = 0L

    private val executor by lazy {
        if (SystemInfo.OS.contains("win")) {
            XInputExecutor()
        } else {
            LinuxInputExecutor()
        }
    }

    private val receiver = UdpReceiver(8082) { buttons, lx, ly, rx, ry, lt, rt ->
        executor.submit(buttons, lx, ly, rx, ry, lt, rt)
        val now = System.currentTimeMillis()
        if (now - lastUiUpdate > 16) {
            lastUiUpdate = now
            onEvent(buttons, lx, ly, rx, ry, lt, rt)
        }
    }

    val discovery = DiscoveryServer(port = 8083)

    init {
        receiver.start()
        discovery.start()
        when (executor) {
            is XInputExecutor -> {
                (executor as XInputExecutor).onRumble = { large: Int, small: Int ->
                    receiver.onRumble(large, small)
                }
            }
            else -> println("${executor.javaClass.name}: Rumble is not supported yet")
        }
        discovery.onResponded = { features ->
            receiver.setEnabledFeatures(features)
        }
    }

    fun onEvent(buttons: Int, lx: Short, ly: Short, rx: Short, ry: Short, lt: Byte, rt: Byte) {
        _lastState.value = GamepadState(buttons, lx, ly, rx, ry, lt, rt)
    }

    override fun onCleared() {
        receiver.stop()
        executor.shutdown()
    }
}
