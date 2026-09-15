package ph.asana.app.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ph.asana.app.billing.BillingEvent
import ph.asana.app.ui.feedback.FeedbackDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignInClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val productPrice by viewModel.productPrice.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showFeedbackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.billingEvents.collect { event ->
            val message = when (event) {
                BillingEvent.PurchaseVerified -> "You're all set — lifetime access unlocked."
                BillingEvent.VerificationFailed -> "Couldn't verify that purchase. Please try again."
                BillingEvent.PurchaseFailed -> "Purchase didn't go through. Please try again."
                BillingEvent.NothingToRestore -> "No previous purchase found for this account."
            }
            coroutineScope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val user = currentUser
            if (user != null) {
                Text(user.name, style = MaterialTheme.typography.titleMedium)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign out")
                }
            } else {
                Text(
                    "Sign in to save favorites and sync them across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign in")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            if (isPremium) {
                Text("✓ Lifetime access unlocked", style = MaterialTheme.typography.titleMedium)
            } else {
                Text("Unlock everything", style = MaterialTheme.typography.titleMedium)
                Text(
                    "All services, offline access, no ads — one payment, yours for good.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        if (user == null) {
                            onSignInClick()
                        } else {
                            viewModel.upgrade(context as Activity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(productPrice?.let { "Unlock for $it" } ?: "Unlock Lifetime Access")
                }
                TextButton(onClick = viewModel::restorePurchases, modifier = Modifier.fillMaxWidth()) {
                    Text("Restore purchase")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            OutlinedButton(onClick = { showFeedbackDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Report a problem")
            }
        }
    }

    if (showFeedbackDialog) {
        FeedbackDialog(
            context = "Settings",
            onDismiss = { showFeedbackDialog = false },
            onSubmitted = {
                showFeedbackDialog = false
                coroutineScope.launch { snackbarHostState.showSnackbar("Thanks — we'll take a look.") }
            },
        )
    }
}
