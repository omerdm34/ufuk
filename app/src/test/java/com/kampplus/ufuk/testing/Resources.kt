package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.network.di.NetworkModule
import com.kampplus.ufuk.feature.forecast.data.remote.dto.AirQualityResponseDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto

fun readResource(name: String): String = checkNotNull(object {}.javaClass.classLoader?.getResource(name)) {
    "Missing test resource $name"
}.readText()

val testJson = NetworkModule.provideJson()

/** Ankara, 27 Eylül 2026 14:15 — forecast_full.json + air_quality.json. */
fun ankaraPayload(): ForecastPayload = ForecastPayload(
    forecast = testJson.decodeFromString(ForecastResponseDto.serializer(), readResource("forecast_full.json")),
    airQuality = testJson.decodeFromString(AirQualityResponseDto.serializer(), readResource("air_quality.json"))
)
