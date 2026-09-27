package com.kampplus.ufuk.feature.forecast.presentation

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.data.mapper.toDomain
import com.kampplus.ufuk.testing.ankara
import com.kampplus.ufuk.testing.ankaraPayload
import com.kampplus.ufuk.testing.testForecastUiMapper
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastUiMapperTest {

    private val mapper = testForecastUiMapper()
    private val forecast = ankaraPayload().toDomain()
    private val turkish = Locale.forLanguageTag("tr")

    private fun model(units: UnitSettings = UnitSettings()) = mapper.toUiModel(ankara, forecast, units, turkish)

    @Test
    fun `dial scale snaps to fives around today, now and yesterday`() = with(model().dial) {
        assertEquals("22°", nowText)
        assertEquals(5f, scaleMin)
        assertEquals(30f, scaleMax)
        assertEquals(12f, todayMin)
        assertEquals(18.2f, yesterday!!, 0.01f)
        assertEquals(UiText.Resource(R.string.detail_yesterday_legend, "18°"), yesterdayLegend)
    }

    @Test
    fun `fahrenheit converts every value on the dial`() = with(model(UnitSettings(temperature = TemperatureUnit.Fahrenheit)).dial) {
        assertEquals("72°", nowText)
        assertEquals(72.3f, now, 0.1f)
        assertEquals(0, scaleMin.toInt() % majorStep)
    }

    @Test
    fun `summary leads with the rain and compares with yesterday and tomorrow`() {
        val insights = model().insights

        assertEquals(UiText.Resource(R.string.insight_rain_starting, "15:00", UiText.Resource(R.string.percent_value, 60)), insights[0])
        assertEquals(UiText.Resource(R.string.insight_warmer_than_yesterday, 4), insights[1])
        assertEquals(UiText.Resource(R.string.insight_tomorrow_colder, 4), insights[2])
    }

    @Test
    fun `hourly starts with the live reading`() {
        val hourly = model().hourly

        assertEquals(UiText.Resource(R.string.hour_now), hourly.first().label)
        assertEquals("22°", hourly.first().temperatureText)
        assertEquals(UiText.Dynamic("15:00"), hourly[1].label)
    }

    @Test
    fun `days start today and share one scale`() {
        val daily = model().daily

        assertEquals(listOf(UiText.Resource(R.string.day_today), UiText.Resource(R.string.day_tomorrow)), daily.days.map { it.label })
        assertEquals(10.4f, daily.scaleMin, 0.01f)
        assertEquals(23.5f, daily.scaleMax, 0.01f)
        assertEquals(UiText.Resource(R.string.percent_value, 70), daily.days.first().precipitation)
        assertEquals(UiText.Resource(R.string.percent_value, 20), daily.days[1].precipitation)
    }

    @Test
    fun `instruments read pressure trend, wind origin and air quality`() = with(model().instruments) {
        assertEquals(UiText.Resource(R.string.pressure_falling), pressure!!.note)
        assertEquals(1011.6f, pressure.previous!!, 0.01f)
        assertEquals(45f, wind!!.direction)
        assertEquals(UiText.Resource(R.string.aqi_fair), (airQuality!!.note as UiText.Resource).args.first())
        assertTrue(sun!!.progress!! in 0.6f..0.62f)
    }

    @Test
    fun `unnamed device location is called my location`() {
        val here = Place(Place.DEVICE_LOCATION_ID, "", null, null, ankara.coordinates)

        assertEquals(UiText.Resource(R.string.my_location), mapper.placeTitle(here))
        assertNotNull(mapper.toUiModel(here, forecast, UnitSettings(), turkish).shareText)
    }
}
