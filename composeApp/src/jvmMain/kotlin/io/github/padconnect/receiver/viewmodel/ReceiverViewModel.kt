
package io.github.padconnect.receiver.viewmodel

import androidx.lifecycle.ViewModel
import io.github.padconnect.receiver.data.GamepadState
import io.github.padconnect.receiver.input.XInputExecutor
import io.github.padconnect.receiver.utils.DiscoveryServer
import io.github.padconnect.receiver.utils.UdpReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReceiverViewModel : ViewModel() {
    private val _lastState = MutableStateFlow<GamepadState?>(null)
    val lastState: StateFlow<GamepadState?> = _lastState

    private val executor = XInputExecutor()

    private val receiver = UdpReceiver(8082) {
        executor.submit(it)
        onEvent(it)
    }

    val discovery = DiscoveryServer(port = 8083)

    init {
        receiver.start()
        discovery.start()
    }

    fun onEvent(state: GamepadState) {
        _lastState.value = state
    }

    override fun onCleared() {
        receiver.stop()
        executor.shutdown()
    }
}
