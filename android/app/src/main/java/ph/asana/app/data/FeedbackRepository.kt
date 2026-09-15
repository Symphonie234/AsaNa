package ph.asana.app.data

import android.os.Build
import ph.asana.app.BuildConfig
import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.FeedbackRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepository @Inject constructor(
    private val api: AsaNaApiService,
) {
    suspend fun submit(message: String, context: String): Result<Unit> =
        runCatching {
            val response = api.submitFeedback(
                FeedbackRequestBody(
                    message = message,
                    context = context,
                    appVersion = BuildConfig.VERSION_NAME,
                    deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}",
                ),
            )
            check(response.isSuccessful) { "Failed to submit feedback: HTTP ${response.code()}" }
        }
}
