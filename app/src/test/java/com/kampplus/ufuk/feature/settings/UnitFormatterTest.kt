package com.kampplus.ufuk.feature.settings

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.core.ui.format.UnitFormatter
import com.kampplus.ufuk.core.ui.text.UiText
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitFormatterTest {

    private val metric = UnitFormatter(UnitSettings())
    private val imperial = UnitFormatter(UnitSettings(TemperatureUnit.Fahrenheit, WindSpeedUnit.MilesPerHour))

    @Test
    fun `temperatures are rounded and converted`() {
        assertEquals("22°", metric.temperature(22.4))
        assertEquals("72°", imperial.temperature(22.4))
        assertEquals("0°", metric.temperature(-0.4))
        assertEquals("32°", imperial.temperature(0.0))
    }

    @Test
    fun `differences convert without the offset`() {
        assertEquals(4, metric.temperatureDelta(-4.2))
        assertEquals(9, imperial.temperatureDelta(5.0))
    }

    @Test
    fun `wind speed uses the chosen unit`() {
        assertEquals(UiText.Resource(R.string.format_value_unit, 20, UiText.Resource(R.string.unit_kmh)), metric.windSpeed(20.0))
        assertEquals(12, imperial.windValue(20.0))
        assertEquals(6, UnitFormatter(UnitSettings(windSpeed = WindSpeedUnit.MetresPerSecond)).windValue(20.0))
    }
}
