package com.kampplus.ufuk.feature.forecast.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Open-Meteo /air-quality yanıtı (yalnızca anlık değerler istenir). */
@Serializable
data class AirQualityResponseDto(
    val current: AirQualityCurrentDto? = null
)

@Serializable
data class AirQualityCurrentDto(
    val time: String,
    @SerialName("european_aqi") val europeanAqi: Int? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null
)
