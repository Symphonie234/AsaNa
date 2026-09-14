package ph.asana.app.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.LoginRequestBody
import ph.asana.app.network.model.RegisterRequestBody
import ph.asana.app.network.model.UserDto
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AsaNaApiService,
    private val tokenStore: TokenStore,
) {
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    val isSignedIn: Boolean get() = tokenStore.token != null

    suspend fun register(name: String, email: String, password: String): Result<UserDto> =
        runCatching {
            val result = api.register(RegisterRequestBody(name, email, password, password)).data
            tokenStore.token = result.token
            _currentUser.value = result.user
            result.user
        }

    suspend fun login(email: String, password: String): Result<UserDto> =
        runCatching {
            val result = api.login(LoginRequestBody(email, password)).data
            tokenStore.token = result.token
            _currentUser.value = result.user
            result.user
        }

    suspend fun logout() {
        runCatching { api.logout() }
        tokenStore.clear()
        _currentUser.value = null
    }

    /**
     * Hydrates [currentUser] from a stored token on app start. Only clears
     * the token on an actual 401 from the server — a network error here
     * just means we don't know the user yet, not that they're logged out.
     */
    suspend fun refreshCurrentUser() {
        if (!isSignedIn) return
        try {
            _currentUser.value = api.me().data
        } catch (e: HttpException) {
            if (e.code() == 401) {
                tokenStore.clear()
                _currentUser.value = null
            }
        } catch (e: Exception) {
            // Network/parsing issue — leave the stored token alone and retry later.
        }
    }
}
