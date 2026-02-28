
package io.github.padconnect.receiver.data

data class GamepadState(
    var buttons: Int = 0,
    var lx: Short = 0,
    var ly: Short = 0,
    var rx: Short = 0,
    var ry: Short = 0,
    var lt: Byte = 0,
    var rt: Byte = 0
)
