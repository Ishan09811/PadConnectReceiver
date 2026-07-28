
package io.github.padconnect.receiver.data

data class GamepadState(
    @Volatile var buttons: Int = 0,
    @Volatile var lx: Short = 0,
    @Volatile var ly: Short = 0,
    @Volatile var rx: Short = 0,
    @Volatile var ry: Short = 0,
    @Volatile var lt: Byte = 0,
    @Volatile var rt: Byte = 0
)
