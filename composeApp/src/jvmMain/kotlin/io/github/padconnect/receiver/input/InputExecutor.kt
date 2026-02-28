
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadState

interface InputExecutor {
    fun submit(state: GamepadState)
    fun shutdown()
}
