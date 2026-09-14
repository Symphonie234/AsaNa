package ph.asana.app.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ph.asana.app.data.local.CacheDao
import ph.asana.app.data.local.CachedResponse
import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.CityDto
import ph.asana.app.network.model.ServiceDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsaNaRepository @Inject constructor(
    private val api: AsaNaApiService,
    private val cacheDao: CacheDao,
    private val json: Json,
) {
    suspend fun getCities(): Result<List<CityDto>> =
        networkFirst("cities", ListSerializer(CityDto.serializer())) { api.getCities().data }

    suspend fun getCategories(): Result<List<CategoryDto>> =
        networkFirst("categories", ListSerializer(CategoryDto.serializer())) { api.getCategories().data }

    suspend fun getServices(city: String? = null, category: String? = null): Result<List<ServiceDto>> =
        networkFirst("services:city=$city&category=$category", ListSerializer(ServiceDto.serializer())) {
            api.getServices(city, category).data
        }

    suspend fun getService(slug: String): Result<ServiceDto> =
        networkFirst("service:$slug", ServiceDto.serializer()) { api.getService(slug).data }

    suspend fun search(query: String): Result<List<ServiceDto>> =
        networkFirst("search:$query", ListSerializer(ServiceDto.serializer())) { api.search(query).data }

    /**
     * Tries the network first and refreshes the cache on success. If the
     * network call fails because there's no connection, falls back to
     * whatever was last cached under [key] rather than failing outright —
     * that's the "still usable with poor signal outside an office" behavior
     * offline access is for. A non-network failure (e.g. a 404 or a
     * malformed response) is a real error and is not masked by stale cache.
     */
    private suspend fun <T> networkFirst(key: String, serializer: KSerializer<T>, networkCall: suspend () -> T): Result<T> =
        try {
            val result = networkCall()
            cacheDao.put(CachedResponse(key, json.encodeToString(serializer, result), System.currentTimeMillis()))
            Result.success(result)
        } catch (e: IOException) {
            cacheDao.get(key)?.let { Result.success(json.decodeFromString(serializer, it.json)) } ?: Result.failure(e)
        }
}
