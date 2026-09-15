package ph.asana.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequestBody(
    val message: String,
    val context: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("device_info") val deviceInfo: String? = null,
)
