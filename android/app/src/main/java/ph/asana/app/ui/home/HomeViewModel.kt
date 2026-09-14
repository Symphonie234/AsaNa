package ph.asana.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.data.AsaNaRepository
import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState
import javax.inject.Inject

data class HomeData(
    val categories: List<CategoryDto>,
    val popularServices: List<ServiceDto>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AsaNaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeData>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val categories = repository.getCategories()
            val services = repository.getServices()

            _uiState.value = if (categories.isSuccess && services.isSuccess) {
                UiState.Success(HomeData(categories.getOrThrow(), services.getOrThrow()))
            } else {
                UiState.Error("Couldn't load AsaNa. Check your connection and try again.")
            }
        }
    }
}
