package ph.asana.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DataEnvelope<T>(
    val data: T,
)

@Serializable
data class CityDto(
    val id: Int,
    val name: String,
    val province: String,
    val slug: String,
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
)

@Serializable
data class OfficeHourDto(
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("opens_at") val opensAt: String,
    @SerialName("closes_at") val closesAt: String,
)

@Serializable
data class OfficeDto(
    val id: Int,
    val name: String,
    val address: String,
    val latitude: String? = null,
    val longitude: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    @SerialName("office_hours") val officeHours: List<OfficeHourDto> = emptyList(),
)

@Serializable
data class RequirementDto(
    val name: String,
    val description: String? = null,
    @SerialName("is_required") val isRequired: Boolean,
)

@Serializable
data class ServiceFeeDto(
    val description: String,
    @SerialName("amount_min") val amountMin: String,
    @SerialName("amount_max") val amountMax: String? = null,
    val notes: String? = null,
)

@Serializable
data class ServiceDto(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String? = null,
    val instructions: String? = null,
    @SerialName("is_premium") val isPremium: Boolean,
    @SerialName("verified_at") val verifiedAt: String? = null,
    val source: String? = null,
    val city: CityDto? = null,
    val category: CategoryDto? = null,
    val office: OfficeDto? = null,
    val requirements: List<RequirementDto> = emptyList(),
    val fees: List<ServiceFeeDto> = emptyList(),
)
