package com.kampplus.ufuk.feature.forecast.domain

import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import com.kampplus.ufuk.feature.forecast.domain.policy.FreshnessPolicy
import com.kampplus.ufuk.feature.forecast.domain.policy.WeatherCondition
import com.kampplus.ufuk.feature.forecast.domain.policy.WmoWeatherConditionClassifier
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastPoliciesTest {

    private val classifier = WmoWeatherConditionClassifier()
    private val freshness = FreshnessPolicy()
    private val now = Instant.parse("2026-09-27T12:00:00Z")

    @Test
    fun `wmo codes map to conditions`() {
        assertEquals(WeatherCondition.Clear, classifier.classify(WeatherCode(0)))
        assertEquals(WeatherCondition.Fog, classifier.classify(WeatherCode(48)))
        assertEquals(WeatherCondition.FreezingRain, classifier.classify(WeatherCode(66)))
        assertEquals(WeatherCondition.Thunderstorm, classifier.classify(WeatherCode(99)))
        assertEquals(WeatherCondition.Unknown, classifier.classify(WeatherCode(42)))
    }

    @Test
    fun `only precipitation counts as wet`() {
        assertTrue(WeatherCondition.RainShowers.isWet)
        assertFalse(WeatherCondition.Fog.isWet)
    }

    @Test
    fun `forecast refreshes after ten minutes and turns stale after forty five`() {
        assertTrue(freshness.shouldRefresh(null, now))
        assertFalse(freshness.shouldRefresh(now.minusSeconds(9 * 60), now))
        assertTrue(freshness.shouldRefresh(now.minusSeconds(10 * 60), now))
        assertFalse(freshness.isStale(now.minusSeconds(44 * 60), now))
        assertTrue(freshness.isStale(now.minusSeconds(45 * 60), now))
    }

    @Test
    fun `a clock running behind never produces a negative age`() {
        assertEquals(0L, freshness.age(now.plusSeconds(60), now).seconds)
    }
}
