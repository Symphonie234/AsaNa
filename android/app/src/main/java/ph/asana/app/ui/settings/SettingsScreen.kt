package ph.asana.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSignInClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
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
        }
    }
}
