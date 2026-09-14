package ph.asana.app.network

import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.CityDto
import ph.asana.app.network.model.DataEnvelope
import ph.asana.app.network.model.PagedEnvelope
import ph.asana.app.network.model.ServiceDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AsaNaApiService {

    @GET("cities")
    suspend fun getCities(): DataEnvelope<List<CityDto>>

    @GET("categories")
    suspend fun getCategories(): DataEnvelope<List<CategoryDto>>

    @GET("services")
    suspend fun getServices(
        @Query("city") city: String? = null,
        @Query("category") category: String? = null,
    ): PagedEnvelope<ServiceDto>

    @GET("services/{slug}")
    suspend fun getService(@Path("slug") slug: String): DataEnvelope<ServiceDto>

    @GET("search")
    suspend fun search(@Query("q") query: String): PagedEnvelope<ServiceDto>
}
