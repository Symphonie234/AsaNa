package ph.asana.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyPurchaseRequestBody(
    @SerialName("product_id") val productId: String,
    @SerialName("purchase_token") val purchaseToken: String,
)

@Serializable
data class EntitlementDto(
    @SerialName("product_id") val productId: String,
    val status: String,
    @SerialName("purchased_at") val purchasedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)
