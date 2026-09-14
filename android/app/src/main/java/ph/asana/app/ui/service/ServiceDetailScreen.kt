package ph.asana.app.ui.service

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ph.asana.app.network.model.OfficeDto
import ph.asana.app.network.model.RequirementDto
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.network.model.ServiceFeeDto
import ph.asana.app.ui.UiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DAY_NAMES = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
private val VERIFIED_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy").withZone(ZoneId.systemDefault())

private fun formatVerifiedDate(iso: String): String =
    runCatching { VERIFIED_DATE_FORMAT.format(Instant.parse(iso)) }.getOrDefault(iso)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    onBack: () -> Unit,
    onSignInRequired: () -> Unit,
    viewModel: ServiceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveButtonState by viewModel.saveButtonState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.saveEvents.collect { event ->
            when (event) {
                SaveFavoriteEvent.SignInRequired -> onSignInRequired()
                SaveFavoriteEvent.Saved -> coroutineScope.launch { snackbarHostState.showSnackbar("Saved to favorites") }
                SaveFavoriteEvent.Failed -> coroutineScope.launch { snackbarHostState.showSnackbar("Couldn't save. Try again.") }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details") },
                navigationIcon = { OutlinedButton(onClick = onBack) { Text("Back") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            is UiState.Success -> ServiceDetailContent(
                padding = padding,
                service = state.data,
                saveButtonState = saveButtonState,
                onSave = viewModel::saveFavorite,
            )
        }
    }
}

@Composable
private fun ServiceDetailContent(
    padding: PaddingValues,
    service: ServiceDto,
    saveButtonState: SaveButtonState,
    onSave: () -> Unit,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text(service.name, style = MaterialTheme.typography.headlineSmall) }

        service.description?.let { description ->
            item { Text(description, style = MaterialTheme.typography.bodyMedium) }
        }

        if (service.requirements.isNotEmpty()) {
            item { SectionTitle("Requirements") }
            items(service.requirements) { requirement -> RequirementRow(requirement) }
        }

        service.instructions?.let { instructions ->
            item { SectionTitle("Steps") }
            item { Text(instructions, style = MaterialTheme.typography.bodyMedium) }
        }

        if (service.fees.isNotEmpty()) {
            item { SectionTitle("Fees") }
            items(service.fees) { fee -> FeeRow(fee) }
        }

        service.office?.let { office ->
            item { SectionTitle("Office") }
            item { OfficeSection(office) }
        }

        service.verifiedAt?.let { verifiedAt ->
            item { Text("Last verified: ${formatVerifiedDate(verifiedAt)}", style = MaterialTheme.typography.bodySmall) }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                service.office?.let { office ->
                    Button(onClick = { openDirections(context, office) }) { Text("Directions") }
                    office.phone?.let { phone ->
                        OutlinedButton(onClick = { callOffice(context, phone) }) { Text("Contact") }
                    }
                }
                SaveButton(saveButtonState, onSave)
            }
        }
    }
}

/**
 * Save is a fire-and-forget action with a real network round-trip behind
 * it, so the button needs its own visible state — otherwise a tap can look
 * like it did nothing until a snackbar appears moments later. SAVING shows
 * a spinner in place of the label; SAVED disables the button and shows a
 * checkmark so it's clear the tap registered and succeeded.
 */
@Composable
private fun SaveButton(state: SaveButtonState, onSave: () -> Unit) {
    when (state) {
        SaveButtonState.IDLE -> OutlinedButton(onClick = onSave) { Text("Save") }

        SaveButtonState.SAVING -> OutlinedButton(onClick = {}, enabled = false) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }

        SaveButtonState.SAVED -> Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Saved")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun RequirementRow(requirement: RequirementDto) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = false, onCheckedChange = null, enabled = false)
        Column {
            Text(requirement.name, style = MaterialTheme.typography.bodyMedium)
            requirement.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun FeeRow(fee: ServiceFeeDto) {
    Column {
        val amount = if (fee.amountMax != null) "₱${fee.amountMin}–₱${fee.amountMax}" else "₱${fee.amountMin}"
        Text("${fee.description}: $amount", style = MaterialTheme.typography.bodyMedium)
        fee.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun OfficeSection(office: OfficeDto) {
    Column {
        Text(office.name, style = MaterialTheme.typography.bodyMedium)
        Text(office.address, style = MaterialTheme.typography.bodySmall)
        if (office.officeHours.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            office.officeHours.sortedBy { it.dayOfWeek }.forEach { hour ->
                Text(
                    "${DAY_NAMES[hour.dayOfWeek]}: ${hour.opensAt}–${hour.closesAt}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun openDirections(context: android.content.Context, office: OfficeDto) {
    val query = if (office.latitude != null && office.longitude != null) {
        "${office.latitude},${office.longitude}"
    } else {
        office.address
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
    context.startActivity(intent)
}

private fun callOffice(context: android.content.Context, phone: String) {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}
