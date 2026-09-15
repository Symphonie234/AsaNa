package ph.asana.app.ui.feedback

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
import ph.asana.app.data.FeedbackRepository
import javax.inject.Inject

data class FeedbackUiState(
    val message: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface FeedbackEvent {
    data object Submitted : FeedbackEvent
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>()
    val events = _events.asSharedFlow()

    fun onMessageChange(value: String) = _uiState.update { it.copy(message = value, error = null) }

    fun submit(context: String) {
        val message = _uiState.value.message.trim()
        if (message.isEmpty()) {
            _uiState.update { it.copy(error = "Let us know what happened before sending.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            feedbackRepository.submit(message, context).fold(
                onSuccess = {
                    _uiState.value = FeedbackUiState()
                    _events.emit(FeedbackEvent.Submitted)
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Couldn't send that. Check your connection and try again.")
                    }
                },
            )
        }
    }
}
