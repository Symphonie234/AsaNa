package ph.asana.app.network

import ph.asana.app.network.model.AuthResultDto
import ph.asana.app.network.model.CategoryDto
import ph.asana.app.network.model.CityDto
import ph.asana.app.network.model.DataEnvelope
import ph.asana.app.network.model.FavoriteRequestBody
import ph.asana.app.network.model.LoginRequestBody
import ph.asana.app.network.model.PagedEnvelope
import ph.asana.app.network.model.RegisterRequestBody
import ph.asana.app.network.model.ServiceDto
import ph.asana.app.network.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestBody): DataEnvelope<AuthResultDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestBody): DataEnvelope<AuthResultDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("me")
    suspend fun me(): DataEnvelope<UserDto>

    @GET("me/favorites")
    suspend fun getFavorites(): PagedEnvelope<ServiceDto>

    @POST("me/favorites")
    suspend fun addFavorite(@Body body: FavoriteRequestBody): PagedEnvelope<ServiceDto>

    @DELETE("me/favorites/{slug}")
    suspend fun removeFavorite(@Path("slug") slug: String): Response<Unit>
}
