package com.kampplus.ufuk.feature.forecast.data

import com.kampplus.ufuk.feature.forecast.data.mapper.toDomain
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto
import com.kampplus.ufuk.feature.forecast.domain.model.hoursAgo
import com.kampplus.ufuk.feature.forecast.domain.model.sameHourYesterday
import com.kampplus.ufuk.feature.forecast.domain.model.today
import com.kampplus.ufuk.feature.forecast.domain.model.tomorrow
import com.kampplus.ufuk.feature.forecast.domain.model.upcomingDays
import com.kampplus.ufuk.feature.forecast.domain.model.upcomingHours
import com.kampplus.ufuk.testing.ankaraPayload
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastMapperTest {

    private val forecast = ankaraPayload().toDomain()

    @Test
    fun `current conditions keep every measured value`() = with(forecast.current) {
        assertEquals(LocalDateTime.of(2026, 9, 27, 14, 15), time)
        assertEquals(1009.8, pressureHpa!!, 0.0)
        assertEquals(225, windDirectionDegrees)
        assertEquals(10.9, dewPointC!!, 0.0)
        assertTrue(isDay)
    }

    @Test
    fun `hourly rows with a missing temperature are skipped`() {
        assertEquals(6, forecast.hourly.size)
        assertTrue(forecast.hourly.none { it.time.hour == 13 })
    }

    @Test
    fun `yesterday is kept for comparison but days start from today`() {
        assertEquals(18.2, forecast.sameHourYesterday()!!.temperatureC, 0.0)
        assertEquals(LocalDate.of(2026, 9, 27), forecast.upcomingDays().first().date)
        assertEquals(12.0, forecast.today()!!.minTemperatureC, 0.0)
        assertEquals(19.1, forecast.tomorrow()!!.maxTemperatureC, 0.0)
    }

    @Test
    fun `upcoming hours start from the current hour`() {
        assertEquals(listOf(14, 15, 16), forecast.upcomingHours(24).map { it.time.hour })
        assertEquals(1011.6, forecast.hoursAgo(3)!!.pressureHpa!!, 0.0)
    }

    @Test
    fun `sunrise, sunset and air quality are parsed`() {
        assertEquals(LocalDateTime.of(2026, 9, 27, 7, 2), forecast.today()!!.sunrise)
        assertEquals(38, forecast.airQuality!!.europeanAqi)
    }

    @Test(expected = SerializationException::class)
    fun `missing current block is a malformed response`() {
        ForecastPayload(forecast = ForecastResponseDto(latitude = 39.9, longitude = 32.8)).toDomain()
    }
}
