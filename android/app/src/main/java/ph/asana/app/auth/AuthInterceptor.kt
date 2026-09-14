package ph.asana.app.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/** Attaches the stored bearer token to every request, when one exists. */
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStore.token ?: return chain.proceed(request)

        return chain.proceed(request.newBuilder().addHeader("Authorization", "Bearer $token").build())
    }
}
