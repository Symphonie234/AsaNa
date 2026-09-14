package ph.asana.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.billing.BillingRepository
import ph.asana.app.ui.navigation.AsaNaNavHost
import ph.asana.app.ui.theme.AsaNaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    // Field injection alone is enough to construct this singleton and start
    // its Play Billing connection — see BillingRepository's class doc for
    // why it manages its own lifecycle instead of a ViewModel's.
    @Inject
    lateinit var billingRepository: BillingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { authRepository.refreshCurrentUser() }

        setContent {
            AsaNaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AsaNaNavHost()
                }
            }
        }
    }
}
