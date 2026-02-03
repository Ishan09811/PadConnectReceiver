
package io.github.padconnect.receiver.viewmodel

import androidx.lifecycle.ViewModel
import io.github.padconnect.receiver.data.GamepadEvent
import io.github.padconnect.receiver.input.InputExecutor
import io.github.padconnect.receiver.utils.DiscoveryServer
import io.github.padconnect.receiver.input.XInputExecutor
import io.github.padconnect.receiver.utils.UdpReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReceiverViewModel : ViewModel() {
    private val _lastEvent = MutableStateFlow<GamepadEvent?>(null)
    val lastEvent: StateFlow<GamepadEvent?> = _lastEvent

    private val executor = XInputExecutor()

    private val receiver = UdpReceiver(8082) {
        onEvent(it)
        executor.submit(it)
    }

    val discovery = DiscoveryServer(port = 8083)

    init {
        receiver.start()
        discovery.start()
    }

    fun onEvent(event: GamepadEvent) {
        _lastEvent.value = event
    }

    override fun onCleared() {
        receiver.stop()
        //executor.shutdown()
    }
}
