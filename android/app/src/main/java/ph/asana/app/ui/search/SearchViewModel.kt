package ph.asana.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.data.AsaNaRepository
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 400L

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AsaNaRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<ServiceDto>>>(UiState.Success(emptyList()))
    val uiState: StateFlow<UiState<List<ServiceDto>>> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.value = UiState.Success(emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(SEARCH_DEBOUNCE_MS)
            repository.search(newQuery).fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error("Search failed. Try again.") },
            )
        }
    }
}
