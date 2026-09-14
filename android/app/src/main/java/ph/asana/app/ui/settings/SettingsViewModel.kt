package ph.asana.app.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.billing.BillingEvent
import ph.asana.app.billing.BillingRepository
import ph.asana.app.network.model.UserDto
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val billingRepository: BillingRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserDto?> = authRepository.currentUser
    val isPremium: StateFlow<Boolean> = billingRepository.isPremium
    val productPrice: StateFlow<String?> = billingRepository.productPrice
    val billingEvents: SharedFlow<BillingEvent> = billingRepository.events

    fun signOut() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun upgrade(activity: Activity) {
        billingRepository.launchPurchaseFlow(activity)
    }

    fun restorePurchases() {
        billingRepository.restorePurchases()
    }
}
