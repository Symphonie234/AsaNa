package ph.asana.app.data

import ph.asana.app.network.AsaNaApiService
import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.CityDto
import ph.asana.app.network.model.ServiceDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsaNaRepository @Inject constructor(
    private val api: AsaNaApiService,
) {
    suspend fun getCities(): Result<List<CityDto>> = runCatching { api.getCities().data }

    suspend fun getCategories(): Result<List<CategoryDto>> = runCatching { api.getCategories().data }

    suspend fun getServices(city: String? = null, category: String? = null): Result<List<ServiceDto>> =
        runCatching { api.getServices(city = city, category = category).data }

    suspend fun getService(slug: String): Result<ServiceDto> = runCatching { api.getService(slug).data }

    suspend fun search(query: String): Result<List<ServiceDto>> = runCatching { api.search(query).data }
}
