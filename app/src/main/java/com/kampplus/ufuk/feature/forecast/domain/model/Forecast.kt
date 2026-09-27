package com.kampplus.ufuk.feature.forecast.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * WMO hava durumu kodu (0 açık, 61 yağmur, 95 fırtına…).
 * Ham kod domain'de korunur; nasıl yorumlanacağına `WeatherConditionClassifier` karar verir.
 */
@JvmInline
value class WeatherCode(
    val value: Int
)

/**
 * Bir yerin tahmini. Tüm zamanlar o yerin yerel saatindedir ve "şimdi" [current].time'dır;
 * böylece hesaplar cihazın saat diliminden bağımsız kalır.
 * [hourly] ve [daily] dünü de içerir (dünle karşılaştırma için).
 */
data class Forecast(
    val current: CurrentConditions,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val airQuality: AirQuality? = null
)

data class CurrentConditions(
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: WeatherCode,
    val isDay: Boolean,
    val apparentTemperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val dewPointC: Double? = null,
    val pressureHpa: Double? = null,
    val windSpeedKmh: Double? = null,
    val windGustsKmh: Double? = null,
    val windDirectionDegrees: Int? = null,
    val uvIndex: Double? = null,
    val precipitationMm: Double? = null
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: WeatherCode,
    val isDay: Boolean = true,
    val precipitationProbability: Int? = null,
    val pressureHpa: Double? = null,
    val uvIndex: Double? = null,
    val windSpeedKmh: Double? = null
)

data class DailyForecast(
    val date: LocalDate,
    val weatherCode: WeatherCode,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val precipitationProbability: Int? = null,
    val precipitationSumMm: Double? = null,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
    val uvIndexMax: Double? = null,
    val windSpeedMaxKmh: Double? = null
)

/** Avrupa hava kalitesi indeksi (0 = çok iyi, 100+ = son derece kötü) ve partikül değerleri (µg/m³). */
data class AirQuality(
    val europeanAqi: Int,
    val pm25: Double? = null,
    val pm10: Double? = null
)

/** Önbellekten okunan tahmin ve ne zaman alındığı. Tazelik kararı bu zamana göre verilir. */
data class ForecastSnapshot(
    val forecast: Forecast,
    val fetchedAt: Instant
)
