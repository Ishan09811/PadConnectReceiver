
package io.github.padconnect.receiver.main.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.padconnect.receiver.viewmodel.ReceiverViewModel

@Composable
fun ReceiverScreen(viewModel: ReceiverViewModel) {
    val lastState by viewModel.lastState.collectAsState()

    Text(
        text = lastState?.toString() ?: "Waiting for input..."
    )
}
