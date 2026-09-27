package com.kampplus.ufuk.feature.forecast.domain

import com.kampplus.ufuk.feature.forecast.domain.policy.AirQualityBand
import com.kampplus.ufuk.feature.forecast.domain.policy.CompassPoint
import com.kampplus.ufuk.feature.forecast.domain.policy.PressureTrend
import com.kampplus.ufuk.feature.forecast.domain.policy.UvBand
import org.junit.Assert.assertEquals
import org.junit.Test

class BandsTest {

    @Test
    fun `uv bands follow the who scale`() {
        assertEquals(UvBand.Low, UvBand.of(2.9))
        assertEquals(UvBand.Moderate, UvBand.of(3.0))
        assertEquals(UvBand.High, UvBand.of(7.9))
        assertEquals(UvBand.VeryHigh, UvBand.of(10.0))
        assertEquals(UvBand.Extreme, UvBand.of(11.0))
    }

    @Test
    fun `air quality bands follow the european index`() {
        assertEquals(AirQualityBand.Good, AirQualityBand.of(19))
        assertEquals(AirQualityBand.Fair, AirQualityBand.of(38))
        assertEquals(AirQualityBand.Poor, AirQualityBand.of(60 + 19))
        assertEquals(AirQualityBand.ExtremelyPoor, AirQualityBand.of(140))
    }

    @Test
    fun `pressure trend needs a change of one hectopascal`() {
        assertEquals(PressureTrend.Falling, PressureTrend.of(1009.8, 1011.6))
        assertEquals(PressureTrend.Steady, PressureTrend.of(1012.4, 1012.0))
        assertEquals(PressureTrend.Rising, PressureTrend.of(1015.0, 1013.9))
    }

    @Test
    fun `wind direction snaps to the nearest compass point`() {
        assertEquals(CompassPoint.North, CompassPoint.of(350))
        assertEquals(CompassPoint.North, CompassPoint.of(0))
        assertEquals(CompassPoint.SouthWest, CompassPoint.of(225))
        assertEquals(CompassPoint.East, CompassPoint.of(100))
        assertEquals(CompassPoint.NorthWest, CompassPoint.of(-45))
    }
}
