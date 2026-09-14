package ph.asana.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
)

@Serializable
data class AuthResultDto(
    val token: String,
    val user: UserDto,
)

@Serializable
data class RegisterRequestBody(
    val name: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

@Serializable
data class LoginRequestBody(
    val email: String,
    val password: String,
)

@Serializable
data class FavoriteRequestBody(
    val slug: String,
)
