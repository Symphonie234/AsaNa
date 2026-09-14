package ph.asana.app.ui.service

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.data.AsaNaRepository
import ph.asana.app.data.FavoriteRepository
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState
import javax.inject.Inject

sealed interface SaveFavoriteEvent {
    data object SignInRequired : SaveFavoriteEvent
    data object Saved : SaveFavoriteEvent
    data object Failed : SaveFavoriteEvent
}

/** Drives the Save button's own visual state, separate from the one-off [SaveFavoriteEvent]s. */
enum class SaveButtonState { IDLE, SAVING, SAVED }

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val repository: AsaNaRepository,
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow<UiState<ServiceDto>>(UiState.Loading)
    val uiState: StateFlow<UiState<ServiceDto>> = _uiState.asStateFlow()

    private val _saveButtonState = MutableStateFlow(SaveButtonState.IDLE)
    val saveButtonState: StateFlow<SaveButtonState> = _saveButtonState.asStateFlow()

    private val _saveEvents = MutableSharedFlow<SaveFavoriteEvent>()
    val saveEvents = _saveEvents.asSharedFlow()

    init {
        load()

        // Reflects whether this service is already favorited — without this,
        // opening a service you'd already saved (e.g. tapping it from the
        // Favorites tab itself) would show "Save" as if it weren't, since
        // the button previously only tracked what happened during this one
        // screen visit rather than the actual saved state.
        viewModelScope.launch {
            favoriteRepository.favorites.collect { favorites ->
                if (_saveButtonState.value != SaveButtonState.SAVING) {
                    _saveButtonState.value = if (favorites.any { it.slug == slug }) {
                        SaveButtonState.SAVED
                    } else {
                        SaveButtonState.IDLE
                    }
                }
            }
        }

        // The shared favorites list may not have been populated yet this
        // session (e.g. the user never visited the Favorites tab) — refresh
        // it so the check above has real data to compare against.
        if (authRepository.isSignedIn) {
            viewModelScope.launch { favoriteRepository.refresh() }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getService(slug).fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error("Couldn't load this service.") },
            )
        }
    }

    fun saveFavorite() {
        if (!authRepository.isSignedIn) {
            viewModelScope.launch { _saveEvents.emit(SaveFavoriteEvent.SignInRequired) }
            return
        }

        if (_saveButtonState.value != SaveButtonState.IDLE) return

        viewModelScope.launch {
            _saveButtonState.value = SaveButtonState.SAVING
            val event = favoriteRepository.addFavorite(slug).fold(
                onSuccess = { SaveFavoriteEvent.Saved },
                onFailure = { SaveFavoriteEvent.Failed },
            )
            _saveButtonState.value = if (event == SaveFavoriteEvent.Saved) SaveButtonState.SAVED else SaveButtonState.IDLE
            _saveEvents.emit(event)
        }
    }
}
