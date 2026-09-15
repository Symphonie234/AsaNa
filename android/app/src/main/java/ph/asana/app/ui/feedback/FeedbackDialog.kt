package ph.asana.app.ui.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * A lightweight "report a problem" dialog for Milestone 8 beta testing —
 * testers can flag confusion or bugs from wherever they hit them without
 * needing a Play Store account or bug tracker. [context] identifies which
 * screen it was opened from, so it shows up in the admin feedback inbox.
 */
@Composable
fun FeedbackDialog(
    context: String,
    onDismiss: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FeedbackEvent.Submitted -> onSubmitted()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report a problem") },
        text = {
            Column {
                Text("Tell us what happened or what was confusing. No need to explain everything perfectly.")
                OutlinedTextField(
                    value = uiState.message,
                    onValueChange = viewModel::onMessageChange,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    isError = uiState.error != null,
                    supportingText = uiState.error?.let { { Text(it) } },
                    enabled = !uiState.isLoading,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submit(context) }, enabled = !uiState.isLoading) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isLoading) {
                Text("Cancel")
            }
        },
    )
}
