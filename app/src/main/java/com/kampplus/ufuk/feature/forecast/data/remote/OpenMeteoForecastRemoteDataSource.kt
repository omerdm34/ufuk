package com.kampplus.ufuk.feature.forecast.data.remote

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoAirQualityApi
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoForecastApi
import com.kampplus.ufuk.feature.forecast.data.remote.dto.AirQualityResponseDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class OpenMeteoForecastRemoteDataSource @Inject constructor(
    private val forecastApi: OpenMeteoForecastApi,
    private val airQualityApi: OpenMeteoAirQualityApi
) : ForecastRemoteDataSource {

    /** Tahmin ve hava kalitesi paralel istenir. Hava kalitesi isteğe bağlıdır: düşerse tahmin yine gelir. */
    override suspend fun fetchForecast(coordinates: Coordinates): ForecastPayload = coroutineScope {
        val airQuality = async { fetchAirQualityOrNull(coordinates) }
        val forecast = forecastApi.getForecast(
            latitude = coordinates.latitude(),
            longitude = coordinates.longitude(),
            current = CURRENT_FIELDS,
            hourly = HOURLY_FIELDS,
            daily = DAILY_FIELDS,
            forecastDays = FORECAST_DAYS,
            pastDays = PAST_DAYS
        )
        ForecastPayload(forecast = forecast, airQuality = airQuality.await())
    }

    /** Tüm konumlar tek istekte sorgulanır; tek konumda API dizi yerine nesne döndürür. */
    override suspend fun fetchConditions(coordinates: List<Coordinates>): List<ForecastResponseDto> = when (coordinates.size) {
        0 -> emptyList()
        1 -> listOf(
            coordinates.single().let {
                forecastApi.getForecast(it.latitude(), it.longitude(), CONDITION_FIELDS, daily = RANGE_FIELDS, forecastDays = 1)
            }
        )
        else -> forecastApi.getForecasts(
            latitudes = coordinates.joinToString(",") { it.latitude() },
            longitudes = coordinates.joinToString(",") { it.longitude() },
            current = CONDITION_FIELDS,
            daily = RANGE_FIELDS,
            forecastDays = 1
        )
    }

    private suspend fun fetchAirQualityOrNull(coordinates: Coordinates): AirQualityResponseDto? = try {
        airQualityApi.getAirQuality(latitude = coordinates.latitude(), longitude = coordinates.longitude())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private fun Coordinates.latitude() = latitude.toString()

    private fun Coordinates.longitude() = longitude.toString()

    private companion object {
        // Yeni bir değişken (ör. visibility) = buraya alan adı + DTO/domain alanı.
        const val CURRENT_FIELDS = "temperature_2m,weather_code,is_day,apparent_temperature,relative_humidity_2m,dew_point_2m," +
            "pressure_msl,wind_speed_10m,wind_gusts_10m,wind_direction_10m,uv_index,precipitation"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,is_day,precipitation_probability,pressure_msl,uv_index,wind_speed_10m"
        const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum," +
            "sunrise,sunset,uv_index_max,wind_speed_10m_max"
        const val CONDITION_FIELDS = "temperature_2m,weather_code,is_day"
        const val RANGE_FIELDS = "temperature_2m_max,temperature_2m_min"
        const val FORECAST_DAYS = 10

        /** Dünle karşılaştırma (ayar ibresi) ve 3 saatlik basınç eğilimi için dün de istenir. */
        const val PAST_DAYS = 1
    }
}
