
package io.github.padconnect.receiver

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.padconnect.receiver.main.ui.ReceiverScreen
import io.github.padconnect.receiver.viewmodel.ReceiverViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        ReceiverScreen(viewModel = remember { ReceiverViewModel() })
    }
}