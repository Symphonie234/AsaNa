package ph.asana.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onCategoryClick: (CategoryDto) -> Unit,
    onServiceClick: (ServiceDto) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("AsaNa") }) },
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingBox(padding)
            is UiState.Error -> ErrorBox(padding, state.message, onRetry = viewModel::load)
            is UiState.Success -> HomeContent(
                padding = padding,
                data = state.data,
                onSearchClick = onSearchClick,
                onCategoryClick = onCategoryClick,
                onServiceClick = onServiceClick,
            )
        }
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    data: HomeData,
    onSearchClick: () -> Unit,
    onCategoryClick: (CategoryDto) -> Unit,
    onServiceClick: (ServiceDto) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSearchClick),
                enabled = false,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("What do you need?") },
            )
        }

        if (data.popularServices.isNotEmpty()) {
            item { SectionHeader("Popular") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(data.popularServices) { service ->
                        ServiceCard(service, onClick = { onServiceClick(service) })
                    }
                }
            }
        }

        item { SectionHeader("Categories") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(data.categories) { category ->
                    CategoryChip(category, onClick = { onCategoryClick(category) })
                }
            }
        }

        item { SectionHeader("Emergency") }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("National Emergency Hotline", style = MaterialTheme.typography.titleSmall)
                    Text("911", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Local Danao City numbers (police, fire, DRRMO, barangay) will be added once verified.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ServiceCard(service: ServiceDto, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors()) {
        Column(Modifier.padding(16.dp)) {
            Text(service.name, style = MaterialTheme.typography.titleSmall)
            service.category?.let { Text(it.name, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun CategoryChip(category: CategoryDto, onClick: () -> Unit) {
    Card(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            category.name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun LoadingBox(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBox(padding: PaddingValues, message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Tap to retry",
                modifier = Modifier.clickable(onClick = onRetry).padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
