package ph.asana.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.network.model.UserDto
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserDto?> = authRepository.currentUser

    fun signOut() {
        viewModelScope.launch { authRepository.logout() }
    }
}
