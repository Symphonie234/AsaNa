package ph.asana.app.data

import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.FavoriteRequestBody
import ph.asana.app.network.model.ServiceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val api: AsaNaApiService,
) {
    suspend fun getFavorites(): Result<List<ServiceDto>> = runCatching { api.getFavorites().data }

    suspend fun addFavorite(slug: String): Result<List<ServiceDto>> =
        runCatching { api.addFavorite(FavoriteRequestBody(slug)).data }

    suspend fun removeFavorite(slug: String): Result<Unit> =
        runCatching {
            val response = api.removeFavorite(slug)
            check(response.isSuccessful) { "Failed to remove favorite: HTTP ${response.code()}" }
        }
}
