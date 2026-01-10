
package io.github.padconnect.receiver.viewmodel

import androidx.lifecycle.ViewModel
import io.github.padconnect.receiver.data.GamepadEvent
import io.github.padconnect.receiver.utils.InputExecutor
import io.github.padconnect.receiver.utils.UdpReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReceiverViewModel : ViewModel() {
    private val _lastEvent = MutableStateFlow<GamepadEvent?>(null)
    val lastEvent: StateFlow<GamepadEvent?> = _lastEvent

    private val executor = InputExecutor()
    private val receiver = UdpReceiver(8082) {
        onEvent(it)
        executor.submit(it)
    }

    init {
        receiver.start()
    }

    fun onEvent(event: GamepadEvent) {
        _lastEvent.value = event
    }

    override fun onCleared() {
        receiver.stop()
        //executor.shutdown()
    }
}
