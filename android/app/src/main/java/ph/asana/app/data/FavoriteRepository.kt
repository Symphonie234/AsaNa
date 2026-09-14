package ph.asana.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.FavoriteRequestBody
import ph.asana.app.network.model.ServiceDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [favorites] is the single shared source of truth for the signed-in user's
 * favorites, not something each screen fetches independently. Compose
 * Navigation preserves a tab's state when you switch away and back to it
 * (that's normally what you want), which means a screen that only fetched
 * favorites in its own init{} would keep showing a stale list after another
 * screen (e.g. Service Detail) adds one. Routing every mutation through this
 * one StateFlow means any screen collecting it updates immediately,
 * regardless of which screen made the change.
 */
@Singleton
class FavoriteRepository @Inject constructor(
    private val api: AsaNaApiService,
) {
    private val _favorites = MutableStateFlow<List<ServiceDto>>(emptyList())
    val favorites: StateFlow<List<ServiceDto>> = _favorites.asStateFlow()

    suspend fun refresh(): Result<Unit> =
        runCatching { api.getFavorites().data }
            .onSuccess { _favorites.value = it }
            .map {}

    suspend fun addFavorite(slug: String): Result<Unit> =
        runCatching { api.addFavorite(FavoriteRequestBody(slug)).data }
            .onSuccess { _favorites.value = it }
            .map {}

    suspend fun removeFavorite(slug: String): Result<Unit> =
        runCatching {
            val response = api.removeFavorite(slug)
            check(response.isSuccessful) { "Failed to remove favorite: HTTP ${response.code()}" }
        }.onSuccess {
            _favorites.value = _favorites.value.filterNot { it.slug == slug }
        }

    /** Called on sign-out so a subsequent sign-in doesn't briefly show the previous user's list. */
    fun clear() {
        _favorites.value = emptyList()
    }
}
