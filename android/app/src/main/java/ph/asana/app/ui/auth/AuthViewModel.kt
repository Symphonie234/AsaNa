package ph.asana.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import javax.inject.Inject

enum class AuthMode { SIGN_IN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AuthEvent {
    data object SignedIn : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun onModeChange(mode: AuthMode) = _uiState.update { it.copy(mode = mode, error = null) }
    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onPasswordConfirmationChange(value: String) = _uiState.update { it.copy(passwordConfirmation = value) }

    fun submit() {
        val state = _uiState.value

        if (state.mode == AuthMode.REGISTER && state.password != state.passwordConfirmation) {
            _uiState.update { it.copy(error = "Passwords don't match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = when (state.mode) {
                AuthMode.SIGN_IN -> authRepository.login(state.email, state.password)
                AuthMode.REGISTER -> authRepository.register(state.name, state.email, state.password)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.SignedIn)
                },
                onFailure = {
                    val message = when (state.mode) {
                        AuthMode.SIGN_IN -> "Incorrect email or password."
                        AuthMode.REGISTER -> "Couldn't create your account. Check your details and try again."
                    }
                    _uiState.update { it.copy(isLoading = false, error = message) }
                },
            )
        }
    }
}
