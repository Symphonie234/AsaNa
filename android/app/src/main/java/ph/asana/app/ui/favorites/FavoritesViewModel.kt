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
        // Reloads whenever the signed-in user changes, so returning to this
        // tab right after signing in (or out) shows the correct state
        // without the screen having to trigger it manually.
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    _uiState.value = UiState.Success(emptyList())
                } else {
                    load()
                }
            }
        }
    }

    fun load() {
        if (!isSignedIn) {
            _uiState.value = UiState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            favoriteRepository.getFavorites().fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error("Couldn't load your favorites.") },
            )
        }
    }

    fun removeFavorite(slug: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(slug).onSuccess { load() }
        }
    }
}
