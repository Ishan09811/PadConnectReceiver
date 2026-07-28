
package io.github.padconnect.receiver

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import padconnectreceiver.composeapp.generated.resources.Res
import padconnectreceiver.composeapp.generated.resources.icon

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PadConnectReceiver",
        icon = painterResource(Res.drawable.icon)
    ) {
        App()
    }
}