
package io.github.padconnect.receiver.data

import kotlinx.serialization.Serializable

@Serializable
data class GamepadEvent(
    val type: String,
    val key: String,
    val value: Float = 1f,
    val timestamp: Long
)

