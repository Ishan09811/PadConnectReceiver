
package io.github.padconnect.receiver

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.jna.NativeLibrary
import kotlinx.coroutines.runBlocking
import padconnectreceiver.composeapp.generated.resources.Res
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PadConnectReceiver",
    ) {
        Thread { initNativeLibrary() }.start()
        App()
    }
}

fun initNativeLibrary() {
    NativeLibrary.addSearchPath("ViGEmClient", System.getenv("APPDATA") + "\\io.github.padconnect.receiver")
    val dllBytes = runBlocking { Res.readBytes("files/ViGEmClient.dll") }
    val targetDir = File(System.getenv("APPDATA"), "io.github.padconnect.receiver")
    targetDir.mkdirs()
    val dllFile = File(targetDir, "ViGEmClient.dll")
    if (!dllFile.exists()) {
        dllFile.writeBytes(dllBytes)
    }
}