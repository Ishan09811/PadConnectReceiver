
package io.github.padconnect.receiver.data

import kotlinx.serialization.Serializable

@Serializable
data class GamepadEvent(
    val type: String,
    val key: String? = null,
    val axis: String? = null,
    val value: Float? = null,
    val timestamp: Long
)
