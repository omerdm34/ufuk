package com.kampplus.ufuk.feature.places.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sonuç yoksa `results` alanı hiç gelmez; varsayılan boş liste "Boş" durumunu temsil eder. */
@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto> = emptyList()
)

@Serializable
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    /** GeoNames sınıfı: PPL* yerleşim yeri, AIRP havalimanı, MT dağ… */
    @SerialName("feature_code") val featureCode: String? = null,
    @SerialName("admin1") val region: String? = null,
    val country: String? = null
)
