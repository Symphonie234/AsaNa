package ph.asana.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.data.FavoriteRepository
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.ui.UiState
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<ServiceDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<ServiceDto>>> = _uiState.asStateFlow()

    val isSignedIn: Boolean get() = authRepository.isSignedIn

    init {
        // The repository's favorites list is the single shared source of
        // truth — this just reflects it, so a save from any other screen
        // (e.g. Service Detail) shows up here immediately, without needing
        // this screen to be recreated or manually refreshed.
        viewModelScope.launch {
            favoriteRepository.favorites.collect { _uiState.value = UiState.Success(it) }
        }
        viewModelScope.launch {
            authRepository.currentUser.collect { user -> if (user != null) load() }
        }
    }

    fun load() {
        if (!isSignedIn) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            favoriteRepository.refresh().onFailure {
                _uiState.value = UiState.Error("Couldn't load your favorites.")
            }
        }
    }

    fun removeFavorite(slug: String) {
        viewModelScope.launch { favoriteRepository.removeFavorite(slug) }
    }
}
