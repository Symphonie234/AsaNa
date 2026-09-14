package ph.asana.app.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.data.AsaNaRepository
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: AsaNaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val categoryName: String = checkNotNull(savedStateHandle["name"])
    private val categorySlug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow<UiState<List<ServiceDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ServiceDto>>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getServices(category = categorySlug).fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error("Couldn't load this category.") },
            )
        }
    }
}
