package com.kampplus.ufuk.feature.forecast.domain

import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherInsight
import com.kampplus.ufuk.feature.forecast.domain.policy.InsightGenerator
import com.kampplus.ufuk.feature.forecast.domain.policy.WmoWeatherConditionClassifier
import com.kampplus.ufuk.testing.calmForecast
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightGeneratorTest {

    private val generator = InsightGenerator(WmoWeatherConditionClassifier())

    @Test
    fun `a calm day says nothing changes`() {
        assertEquals(listOf(WeatherInsight.Steady), generator.generate(calmForecast()))
    }

    @Test
    fun `rain starting within twelve hours is reported with its hour`() {
        val forecast = calmForecast(hourly = { h, base ->
            if (h >= 5) base.copy(weatherCode = WeatherCode(61), precipitationProbability = 70) else base
        })

        assertEquals(WeatherInsight.PrecipitationStarting(LocalTime.of(15, 0), 70, isSnow = false), generator.generate(forecast).first())
    }

    @Test
    fun `unlikely rain is not announced`() {
        val forecast = calmForecast(hourly = { h, base ->
            if (h >= 2) base.copy(weatherCode = WeatherCode(61), precipitationProbability = 30) else base
        })

        assertEquals(listOf(WeatherInsight.Steady), generator.generate(forecast))
    }

    @Test
    fun `snow is named as snow`() {
        val forecast = calmForecast(hourly = { h, base ->
            if (h >= 3) base.copy(weatherCode = WeatherCode(73), precipitationProbability = 80) else base
        })

        assertTrue((generator.generate(forecast).first() as WeatherInsight.PrecipitationStarting).isSnow)
    }

    @Test
    fun `while raining it tells when the rain eases`() {
        val forecast = calmForecast(currentCode = 63, hourly = { h, base ->
            if (h < 3) base.copy(weatherCode = WeatherCode(63), precipitationProbability = 90) else base
        })

        assertEquals(WeatherInsight.PrecipitationEasing(LocalTime.of(13, 0)), generator.generate(forecast).first())
    }

    @Test
    fun `strong gusts outrank comparisons`() {
        val forecast = calmForecast(gusts = 62.0, currentTemperature = 26.0, hourly = { h, base ->
            if (h <= -20) base.copy(temperatureC = 20.0) else base
        })

        val insights = generator.generate(forecast)
        assertEquals(WeatherInsight.StrongWind(62.0), insights.first())
        assertTrue(insights.any { it is WeatherInsight.ComparedToYesterday })
    }

    @Test
    fun `frost ahead is reported with the coldest hour`() {
        val forecast = calmForecast(currentTemperature = 4.0, apparent = 4.0, hourly = { h, base ->
            when {
                h == 10 -> base.copy(temperatureC = -2.0)
                h > 0 -> base.copy(temperatureC = 1.0)
                else -> base.copy(temperatureC = 4.0)
            }
        })

        assertEquals(WeatherInsight.FrostAhead(-2.0, LocalTime.of(20, 0)), generator.generate(forecast).first())
    }

    @Test
    fun `high uv reports the window for today only`() {
        val forecast = calmForecast(hourly = { h, base -> if (h in 2..5) base.copy(uvIndex = 7.5) else base })

        assertEquals(WeatherInsight.HighUv(LocalTime.of(12, 0), LocalTime.of(16, 0), 7.5), generator.generate(forecast).first())
    }

    @Test
    fun `yesterday and tomorrow are compared beyond small changes`() {
        val forecast = calmForecast(currentTemperature = 16.0, apparent = 16.0, tomorrowMax = 19.0, hourly = { h, base ->
            if (h == -24) base.copy(temperatureC = 21.0) else base.copy(temperatureC = 16.0)
        })

        assertEquals(
            listOf(WeatherInsight.ComparedToYesterday(-5.0), WeatherInsight.TomorrowChange(-5.0)),
            generator.generate(forecast)
        )
    }

    @Test
    fun `at most three insights are shown`() {
        val forecast = calmForecast(gusts = 70.0, apparent = 14.0, tomorrowMax = 30.0, hourly = { h, base ->
            when {
                h >= 4 -> base.copy(weatherCode = WeatherCode(61), precipitationProbability = 80)
                h in 1..3 -> base.copy(uvIndex = 8.0)
                else -> base
            }
        })

        assertEquals(InsightGenerator.MAX_INSIGHTS, generator.generate(forecast).size)
    }
}
