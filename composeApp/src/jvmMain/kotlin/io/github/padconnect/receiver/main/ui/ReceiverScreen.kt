
package io.github.padconnect.receiver.main.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.padconnect.receiver.viewmodel.ReceiverViewModel

@Composable
fun ReceiverScreen(viewModel: ReceiverViewModel) {
    val lastEvent by viewModel.lastEvent.collectAsState()

    Text(
        text = lastEvent?.toString() ?: "Waiting for input..."
    )
}
