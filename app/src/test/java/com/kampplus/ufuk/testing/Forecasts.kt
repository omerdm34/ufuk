package com.kampplus.ufuk.testing

import com.kampplus.ufuk.feature.forecast.domain.model.CurrentConditions
import com.kampplus.ufuk.feature.forecast.domain.model.DailyForecast
import com.kampplus.ufuk.feature.forecast.domain.model.Forecast
import com.kampplus.ufuk.feature.forecast.domain.model.HourlyForecast
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import java.time.LocalDateTime

val NOW: LocalDateTime = LocalDateTime.of(2026, 9, 27, 10, 20)

/**
 * Sakin bir gün: dünle aynı sıcaklık, yağış yok, UV düşük. Testler yalnızca inceledikleri
 * kuralın girdisini değiştirir; böylece başka bir kural yanlışlıkla tetiklenmez.
 */
fun calmForecast(
    currentTemperature: Double = 20.0,
    currentCode: Int = 1,
    apparent: Double? = 20.0,
    gusts: Double? = 15.0,
    hourly: (hoursFromNow: Int, base: HourlyForecast) -> HourlyForecast = { _, base -> base },
    todayMax: Double = 24.0,
    tomorrowMax: Double = 24.0
): Forecast {
    val startOfYesterday = NOW.toLocalDate().minusDays(1).atStartOfDay()
    val currentHour = NOW.withMinute(0)
    return Forecast(
        current = CurrentConditions(
            time = NOW,
            temperatureC = currentTemperature,
            weatherCode = WeatherCode(currentCode),
            isDay = true,
            apparentTemperatureC = apparent,
            windSpeedKmh = 10.0,
            windGustsKmh = gusts,
            pressureHpa = 1012.0
        ),
        hourly = List(72) { index ->
            val time = startOfYesterday.plusHours(index.toLong())
            val base = HourlyForecast(
                time = time,
                temperatureC = currentTemperature,
                weatherCode = WeatherCode(1),
                isDay = time.hour in 7..18,
                precipitationProbability = 0,
                pressureHpa = 1012.0,
                uvIndex = 2.0
            )
            hourly(java.time.Duration.between(currentHour, time).toHours().toInt(), base)
        },
        daily = listOf(
            daily(NOW.toLocalDate().minusDays(1), max = 24.0),
            daily(NOW.toLocalDate(), max = todayMax),
            daily(NOW.toLocalDate().plusDays(1), max = tomorrowMax)
        )
    )
}

private fun daily(date: java.time.LocalDate, max: Double) = DailyForecast(
    date = date,
    weatherCode = WeatherCode(1),
    minTemperatureC = 12.0,
    maxTemperatureC = max,
    sunrise = date.atTime(7, 2),
    sunset = date.atTime(18, 53)
)
