package com.kampplus.ufuk.core.model

/**
 * Kullanıcının seçtiği birimler. Veri her zaman metrik (°C, km/sa) saklanır ve istenir;
 * dönüşüm yalnızca gösterirken yapılır, böylece önbellek birim değişince geçersiz olmaz.
 */
data class UnitSettings(
    val temperature: TemperatureUnit = TemperatureUnit.Celsius,
    val windSpeed: WindSpeedUnit = WindSpeedUnit.KilometresPerHour
)

enum class TemperatureUnit {
    Celsius,
    Fahrenheit;

    fun fromCelsius(celsius: Double): Double = when (this) {
        Celsius -> celsius
        Fahrenheit -> celsius * FAHRENHEIT_PER_CELSIUS + FAHRENHEIT_OFFSET
    }

    /** Sıcaklık farkı ("dünden 4° sıcak") ofset olmadan çevrilir. */
    fun deltaFromCelsius(delta: Double): Double = when (this) {
        Celsius -> delta
        Fahrenheit -> delta * FAHRENHEIT_PER_CELSIUS
    }

    private companion object {
        const val FAHRENHEIT_PER_CELSIUS = 9.0 / 5.0
        const val FAHRENHEIT_OFFSET = 32.0
    }
}

enum class WindSpeedUnit {
    KilometresPerHour,
    MetresPerSecond,
    MilesPerHour;

    fun fromKmh(kmh: Double): Double = when (this) {
        KilometresPerHour -> kmh
        MetresPerSecond -> kmh / KMH_PER_MS
        MilesPerHour -> kmh / KMH_PER_MPH
    }

    private companion object {
        const val KMH_PER_MS = 3.6
        const val KMH_PER_MPH = 1.609344
    }
}
