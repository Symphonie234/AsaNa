package ph.asana.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.asana.app.auth.AuthRepository
import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.VerifyPurchaseRequestBody
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BillingEvent {
    data object PurchaseVerified : BillingEvent
    data object VerificationFailed : BillingEvent
    data object PurchaseFailed : BillingEvent
    data object NothingToRestore : BillingEvent
}

/**
 * Owns the Play Billing connection and the resulting purchase flow.
 *
 * Unlike the other repositories, this one keeps its own [CoroutineScope]
 * rather than relying on a ViewModel's — the BillingClient connection needs
 * to live for the whole app process, and [onPurchasesUpdated] is a plain
 * callback invoked by the Play Billing library itself, not something a
 * ViewModel action triggers, so there's no natural caller scope to borrow.
 *
 * [isPremium] is driven by our own backend (`GET /billing/entitlements`),
 * never by what Play Billing reports locally — the whole point of
 * server-side verification is that the client's own claim about its
 * purchase state is never the source of truth.
 */
@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val api: AsaNaApiService,
    private val authRepository: AuthRepository,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _productPrice = MutableStateFlow<String?>(null)
    val productPrice: StateFlow<String?> = _productPrice.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>()
    val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .build()

    init {
        scope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) refreshEntitlements() else _isPremium.value = false
            }
        }
        connect()
    }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { queryProductDetails() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // The library retries the connection on the next billing call automatically.
            }
        })
    }

    private suspend fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(LIFETIME_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        val result = billingClient.queryProductDetails(params)
        productDetails = result.productDetailsList?.firstOrNull()
        _productPrice.value = productDetails
            ?.oneTimePurchaseOfferDetails
            ?.formattedPrice
    }

    fun launchPurchaseFlow(activity: Activity) {
        val details = productDetails ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build(),
                ),
            )
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun restorePurchases() {
        scope.launch {
            val result = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            )
            if (result.purchasesList.isEmpty()) {
                _events.emit(BillingEvent.NothingToRestore)
            } else {
                result.purchasesList.forEach { handlePurchase(it) }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                scope.launch { _events.emit(BillingEvent.PurchaseFailed) }
            }
            return
        }
        purchases?.forEach { purchase -> scope.launch { handlePurchase(purchase) } }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        val verified = runCatching {
            api.verifyPurchase(VerifyPurchaseRequestBody(LIFETIME_PRODUCT_ID, purchase.purchaseToken))
        }.isSuccess

        if (!verified) {
            _events.emit(BillingEvent.VerificationFailed)
            return
        }

        _isPremium.value = true
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
            )
        }
        _events.emit(BillingEvent.PurchaseVerified)
    }

    private suspend fun refreshEntitlements() {
        runCatching { api.getEntitlements().data }
            .onSuccess { entitlements ->
                _isPremium.value = entitlements.any { it.productId == LIFETIME_PRODUCT_ID && it.status == "active" }
            }
    }

    companion object {
        const val LIFETIME_PRODUCT_ID = "asanaph.lifetime"
    }
}
