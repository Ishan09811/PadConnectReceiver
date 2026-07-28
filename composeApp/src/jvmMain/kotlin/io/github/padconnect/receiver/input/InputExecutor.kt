
package io.github.padconnect.receiver.input

interface InputExecutor {
    fun submit(buttons: Int, lx: Short, ly: Short, rx: Short, ry: Short, lt: Byte, rt: Byte)
    fun shutdown()
}
