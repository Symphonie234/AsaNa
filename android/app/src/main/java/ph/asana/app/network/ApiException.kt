package ph.asana.app.network

import kotlinx.serialization.json.Json
import ph.asana.app.network.model.ApiErrorBody
import retrofit2.HttpException

/** Wraps an HTTP error with the server's actual message, when it has one. */
class ApiException(message: String, cause: Throwable) : Exception(message, cause)

/**
 * Turns an [HttpException] into an [ApiException] carrying the backend's own
 * validation/error message (e.g. "The email has already been taken.")
 * instead of a generic fallback, so the UI can show the real reason a
 * request failed rather than making the user guess.
 */
fun HttpException.toApiException(json: Json, fallback: String): ApiException {
    val body = response()?.errorBody()?.string()
    val message = body?.let { runCatching { json.decodeFromString<ApiErrorBody>(it).message }.getOrNull() }
    return ApiException(message ?: fallback, this)
}
