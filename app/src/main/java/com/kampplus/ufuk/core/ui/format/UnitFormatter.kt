package com.kampplus.ufuk.core.ui.format

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.core.ui.text.UiText
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Metrik değerleri kullanıcının birimine çevirip ekrana hazır metne dönüştürür.
 * Sıcaklık sembolü yalnızca derece işaretidir (Türkçe ve İngilizce hava uygulaması geleneği);
 * hangi birim olduğu Ayarlar'da ve erişilebilirlik metninde açıkça söylenir.
 */
class UnitFormatter(
    val units: UnitSettings
) {
    fun temperatureValue(celsius: Double): Int = units.temperature.fromCelsius(celsius).roundToInt()

    fun temperature(celsius: Double): String = "${temperatureValue(celsius)}°"

    /** İşaretsiz fark: "dünden 4° sıcak" cümlesinde yön kelimeyle söylenir. */
    fun temperatureDelta(deltaCelsius: Double): Int = abs(units.temperature.deltaFromCelsius(deltaCelsius).roundToInt())

    fun windValue(kmh: Double): Int = units.windSpeed.fromKmh(kmh).roundToInt()

    fun windUnit(): UiText = UiText.Resource(
        when (units.windSpeed) {
            WindSpeedUnit.KilometresPerHour -> R.string.unit_kmh
            WindSpeedUnit.MetresPerSecond -> R.string.unit_ms
            WindSpeedUnit.MilesPerHour -> R.string.unit_mph
        }
    )

    fun windSpeed(kmh: Double): UiText = UiText.Resource(R.string.format_value_unit, windValue(kmh), windUnit())
}
