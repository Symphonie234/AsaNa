package ph.asana.app.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onServiceClick: (ServiceDto) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(viewModel.categoryName) }) },
    ) { padding ->
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
                ) { Text("No services in this category yet.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    items(state.data) { service ->
                        ListItem(
                            headlineContent = { Text(service.name) },
                            modifier = Modifier.clickable { onServiceClick(service) },
                        )
                    }
                }
            }
        }
    }
}
