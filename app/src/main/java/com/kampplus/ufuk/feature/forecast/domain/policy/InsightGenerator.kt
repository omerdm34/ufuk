package com.kampplus.ufuk.feature.forecast.domain.policy

import com.kampplus.ufuk.feature.forecast.domain.model.Forecast
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherInsight
import com.kampplus.ufuk.feature.forecast.domain.model.sameHourYesterday
import com.kampplus.ufuk.feature.forecast.domain.model.today
import com.kampplus.ufuk.feature.forecast.domain.model.tomorrow
import com.kampplus.ufuk.feature.forecast.domain.model.upcomingHours
import javax.inject.Inject
import kotlin.math.abs

/**
 * Tahmini "ne yapmalıyım?" sorusuna cevap veren kısa gözlemlere çevirir.
 * Kurallar önem sırasına göre değerlendirilir: önce plan değiştirenler (yağış, rüzgâr, don),
 * sonra dikkat edilecekler (UV, hissedilen), en son karşılaştırmalar. En fazla [MAX_INSIGHTS] döner.
 */
class InsightGenerator @Inject constructor(
    private val classifier: WeatherConditionClassifier
) {
    fun generate(forecast: Forecast): List<WeatherInsight> {
        val rules = listOf(
            ::precipitation,
            ::strongWind,
            ::frost,
            ::highUv,
            ::feelsDifferent,
            ::comparedToYesterday,
            ::tomorrowChange
        )
        return rules.mapNotNull { rule -> rule(forecast) }
            .take(MAX_INSIGHTS)
            .ifEmpty { listOf(WeatherInsight.Steady) }
    }

    private fun precipitation(forecast: Forecast): WeatherInsight? {
        val next = forecast.upcomingHours(LOOKAHEAD_HOURS + 1).drop(1)
        val isWetNow = classifier.classify(forecast.current.weatherCode).isWet
        return if (isWetNow) {
            next.firstOrNull { hour ->
                !classifier.classify(hour.weatherCode).isWet && (hour.precipitationProbability ?: 0) < DRY_PROBABILITY
            }?.let { WeatherInsight.PrecipitationEasing(at = it.time.toLocalTime()) }
        } else {
            next.firstOrNull { hour ->
                classifier.classify(hour.weatherCode).isWet && (hour.precipitationProbability ?: 0) >= WET_PROBABILITY
            }?.let { hour ->
                val condition = classifier.classify(hour.weatherCode)
                WeatherInsight.PrecipitationStarting(
                    at = hour.time.toLocalTime(),
                    probability = hour.precipitationProbability ?: 0,
                    isSnow = condition == WeatherCondition.Snow || condition == WeatherCondition.SnowShowers
                )
            }
        }
    }

    private fun strongWind(forecast: Forecast): WeatherInsight? {
        val gust = forecast.current.windGustsKmh ?: forecast.current.windSpeedKmh ?: return null
        return if (gust >= STRONG_GUST_KMH) WeatherInsight.StrongWind(gustKmh = gust) else null
    }

    private fun frost(forecast: Forecast): WeatherInsight? {
        if (forecast.current.temperatureC <= 0) return null
        val coldest = forecast.upcomingHours(FROST_LOOKAHEAD_HOURS).minByOrNull { it.temperatureC } ?: return null
        return if (coldest.temperatureC <= 0) {
            WeatherInsight.FrostAhead(minimumC = coldest.temperatureC, at = coldest.time.toLocalTime())
        } else {
            null
        }
    }

    private fun highUv(forecast: Forecast): WeatherInsight? {
        val strongHours = forecast.upcomingHours(LOOKAHEAD_HOURS)
            .filter { it.time.toLocalDate() == forecast.current.time.toLocalDate() && (it.uvIndex ?: 0.0) >= HIGH_UV }
        if (strongHours.isEmpty()) return null
        return WeatherInsight.HighUv(
            from = strongHours.first().time.toLocalTime(),
            until = strongHours.last().time.toLocalTime().plusHours(1),
            peak = strongHours.maxOf { it.uvIndex ?: 0.0 }
        )
    }

    private fun feelsDifferent(forecast: Forecast): WeatherInsight? {
        val apparent = forecast.current.apparentTemperatureC ?: return null
        val delta = apparent - forecast.current.temperatureC
        return if (abs(delta) >= FEELS_THRESHOLD_C) WeatherInsight.FeelsDifferent(apparentC = apparent, deltaC = delta) else null
    }

    private fun comparedToYesterday(forecast: Forecast): WeatherInsight? {
        val yesterday = forecast.sameHourYesterday() ?: return null
        val delta = forecast.current.temperatureC - yesterday.temperatureC
        return if (abs(delta) >= YESTERDAY_THRESHOLD_C) WeatherInsight.ComparedToYesterday(deltaC = delta) else null
    }

    private fun tomorrowChange(forecast: Forecast): WeatherInsight? {
        val today = forecast.today() ?: return null
        val tomorrow = forecast.tomorrow() ?: return null
        val delta = tomorrow.maxTemperatureC - today.maxTemperatureC
        return if (abs(delta) >= TOMORROW_THRESHOLD_C) WeatherInsight.TomorrowChange(deltaC = delta) else null
    }

    companion object {
        const val MAX_INSIGHTS = 3
        const val LOOKAHEAD_HOURS = 12
        const val FROST_LOOKAHEAD_HOURS = 18
        const val WET_PROBABILITY = 50
        const val DRY_PROBABILITY = 30
        const val STRONG_GUST_KMH = 50.0
        const val HIGH_UV = 6.0
        const val FEELS_THRESHOLD_C = 3.0
        const val YESTERDAY_THRESHOLD_C = 3.0
        const val TOMORROW_THRESHOLD_C = 4.0
    }
}
