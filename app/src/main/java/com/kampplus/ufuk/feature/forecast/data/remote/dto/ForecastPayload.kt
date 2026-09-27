package com.kampplus.ufuk.feature.forecast.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Bir yenilemede alınan her şey. Önbelleğe bu biçimde (JSON) yazılır; böylece domain modeline
 * çeviri tek bir yerde (mapper) yapılır ve yeni bir alan eklemek şema değişikliği gerektirmez.
 */
@Serializable
data class ForecastPayload(
    val forecast: ForecastResponseDto,
    val airQuality: AirQualityResponseDto? = null
)
