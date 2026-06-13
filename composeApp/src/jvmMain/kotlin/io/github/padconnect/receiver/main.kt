
package io.github.padconnect.receiver

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import padconnectreceiver.composeapp.generated.resources.Res
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PadConnectReceiver",
    ) {
        App()
    }
}