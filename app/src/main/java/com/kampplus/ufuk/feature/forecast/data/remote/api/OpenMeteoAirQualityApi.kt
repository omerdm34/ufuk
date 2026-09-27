package com.kampplus.ufuk.feature.forecast.data.remote.api

import com.kampplus.ufuk.feature.forecast.data.remote.dto.AirQualityResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** https://air-quality-api.open-meteo.com/v1/ — CAMS tabanlı hava kalitesi, API key gerektirmez. */
interface OpenMeteoAirQualityApi {
    @GET("air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("current") current: String = "european_aqi,pm2_5,pm10",
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponseDto
}
