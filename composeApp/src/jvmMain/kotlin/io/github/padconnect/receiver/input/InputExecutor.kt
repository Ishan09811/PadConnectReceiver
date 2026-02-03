
package io.github.padconnect.receiver.input

import io.github.padconnect.receiver.data.GamepadEvent

interface InputExecutor {
    fun submit(event: GamepadEvent)
    fun shutdown()
}
