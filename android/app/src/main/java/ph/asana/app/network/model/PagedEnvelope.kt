package ph.asana.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PagedEnvelope<T>(
    val data: List<T>,
)
