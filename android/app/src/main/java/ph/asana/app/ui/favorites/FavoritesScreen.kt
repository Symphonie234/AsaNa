package ph.asana.app.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onServiceClick: (ServiceDto) -> Unit,
    onSignInClick: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Favorites") }) },
    ) { padding ->
        if (!viewModel.isSignedIn) {
            SignInPrompt(padding, onSignInClick)
            return@Scaffold
        }

        when (val state = uiState) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is UiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(state.message) }

            is UiState.Success -> if (state.data.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { Text("No favorites yet. Save a service to find it here.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(state.data) { service ->
                        ListItem(
                            headlineContent = { Text(service.name) },
                            supportingContent = service.category?.let { { Text(it.name) } },
                            modifier = Modifier.clickable { onServiceClick(service) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeFavorite(service.slug) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove from favorites")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInPrompt(padding: PaddingValues, onSignInClick: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sign in to save and sync your favorites.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onSignInClick, modifier = Modifier.padding(top = 12.dp)) { Text("Sign in") }
        }
    }
}
