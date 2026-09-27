package com.kampplus.ufuk.feature.forecast.data.remote.api

import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** https://api.open-meteo.com/v1/ — API key gerektirmez. */
interface OpenMeteoForecastApi {

    /** Tek konum → tek nesne. */
    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("current") current: String,
        @Query("hourly") hourly: String? = null,
        @Query("daily") daily: String? = null,
        @Query("forecast_days") forecastDays: Int? = null,
        @Query("past_days") pastDays: Int? = null,
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponseDto

    /** Virgülle ayrılmış birden çok konum → konum sırasıyla dizi. */
    @GET("forecast")
    suspend fun getForecasts(
        @Query("latitude") latitudes: String,
        @Query("longitude") longitudes: String,
        @Query("current") current: String,
        @Query("daily") daily: String? = null,
        @Query("forecast_days") forecastDays: Int? = null,
        @Query("timezone") timezone: String = "auto"
    ): List<ForecastResponseDto>
}
