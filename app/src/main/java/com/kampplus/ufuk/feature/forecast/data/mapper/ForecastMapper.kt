package com.kampplus.ufuk.feature.forecast.data.mapper

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.data.remote.dto.AirQualityResponseDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.CurrentDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.DailyDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto
import com.kampplus.ufuk.feature.forecast.data.remote.dto.HourlyDto
import com.kampplus.ufuk.feature.forecast.domain.model.AirQuality
import com.kampplus.ufuk.feature.forecast.domain.model.CurrentConditions
import com.kampplus.ufuk.feature.forecast.domain.model.DailyForecast
import com.kampplus.ufuk.feature.forecast.domain.model.Forecast
import com.kampplus.ufuk.feature.forecast.domain.model.HourlyForecast
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import com.kampplus.ufuk.feature.forecast.domain.model.today
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.SerializationException

/** `current` istenmiş ama gelmemişse yanıt bozuktur → AppError.Parse. */
fun ForecastResponseDto.requireCurrent(): CurrentDto = current ?: throw SerializationException("Forecast response has no current block")

fun ForecastPayload.toDomain(): Forecast = Forecast(
    current = forecast.requireCurrent().toDomain(),
    hourly = forecast.hourly?.toDomain().orEmpty(),
    daily = forecast.daily?.toDomain().orEmpty(),
    airQuality = airQuality?.toDomain()
)

/** Zamanlar `timezone=auto` ile yerin yerel saatinde, ofsetsiz ISO biçiminde gelir. */
fun CurrentDto.toDomain() = CurrentConditions(
    time = LocalDateTime.parse(time),
    temperatureC = temperature,
    weatherCode = WeatherCode(weatherCode),
    isDay = isDay != 0,
    apparentTemperatureC = apparentTemperature,
    humidityPercent = relativeHumidity,
    dewPointC = dewPoint,
    pressureHpa = pressureMsl,
    windSpeedKmh = windSpeed,
    windGustsKmh = windGusts,
    windDirectionDegrees = windDirection,
    uvIndex = uvIndex,
    precipitationMm = precipitation
)

/** Sütun biçimindeki listeleri satırlara çevirir; zorunlu değeri eksik satırlar atlanır. */
fun HourlyDto.toDomain(): List<HourlyForecast> = time.indices.mapNotNull { index ->
    val temperature = temperature.getOrNull(index) ?: return@mapNotNull null
    val code = weatherCode.getOrNull(index) ?: return@mapNotNull null
    HourlyForecast(
        time = LocalDateTime.parse(time[index]),
        temperatureC = temperature,
        weatherCode = WeatherCode(code),
        isDay = isDay.getOrNull(index)?.let { it != 0 } ?: true,
        precipitationProbability = precipitationProbability.getOrNull(index),
        pressureHpa = pressureMsl.getOrNull(index),
        uvIndex = uvIndex.getOrNull(index),
        windSpeedKmh = windSpeed.getOrNull(index)
    )
}

fun DailyDto.toDomain(): List<DailyForecast> = time.indices.mapNotNull { index ->
    val max = temperatureMax.getOrNull(index) ?: return@mapNotNull null
    val min = temperatureMin.getOrNull(index) ?: return@mapNotNull null
    val code = weatherCode.getOrNull(index) ?: return@mapNotNull null
    DailyForecast(
        date = LocalDate.parse(time[index]),
        weatherCode = WeatherCode(code),
        minTemperatureC = min,
        maxTemperatureC = max,
        precipitationProbability = precipitationProbabilityMax.getOrNull(index),
        precipitationSumMm = precipitationSum.getOrNull(index),
        sunrise = sunrise.getOrNull(index)?.let(LocalDateTime::parse),
        sunset = sunset.getOrNull(index)?.let(LocalDateTime::parse),
        uvIndexMax = uvIndexMax.getOrNull(index),
        windSpeedMaxKmh = windSpeedMax.getOrNull(index)
    )
}

fun AirQualityResponseDto.toDomain(): AirQuality? {
    val current = current ?: return null
    val aqi = current.europeanAqi ?: return null
    return AirQuality(europeanAqi = aqi, pm25 = current.pm25, pm10 = current.pm10)
}

/** Çoklu konum yanıtının bir satırı → liste satırı. */
fun ForecastResponseDto.toConditions(place: Place, fetchedAt: Instant): PlaceConditions {
    val current = requireCurrent()
    return PlaceConditions(
        place = place,
        temperatureC = current.temperature,
        weatherCode = WeatherCode(current.weatherCode),
        isDay = current.isDay != 0,
        todayMinC = daily?.temperatureMin?.firstOrNull(),
        todayMaxC = daily?.temperatureMax?.firstOrNull(),
        localTime = LocalDateTime.parse(current.time),
        fetchedAt = fetchedAt
    )
}

/** Ağ yokken liste satırı önbellekteki tam tahminden türetilir. */
fun Forecast.toConditions(place: Place, fetchedAt: Instant): PlaceConditions {
    val today = today()
    return PlaceConditions(
        place = place,
        temperatureC = current.temperatureC,
        weatherCode = current.weatherCode,
        isDay = current.isDay,
        todayMinC = today?.minTemperatureC,
        todayMaxC = today?.maxTemperatureC,
        localTime = current.time,
        fetchedAt = fetchedAt,
        isFromCache = true
    )
}
