package com.kampplus.ufuk.feature.forecast.domain.policy

import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import javax.inject.Inject

/** Ham hava kodunu uygulamanın konuştuğu hava koşuluna eşleyen iş kuralı (strategy). */
fun interface WeatherConditionClassifier {
    fun classify(code: WeatherCode): WeatherCondition
}

enum class WeatherCondition {
    Clear,
    MainlyClear,
    PartlyCloudy,
    Overcast,
    Fog,
    Drizzle,
    FreezingRain,
    Rain,
    Snow,
    RainShowers,
    SnowShowers,
    Thunderstorm,
    Unknown;

    /** Yağış var mı? (Özet cümleleri "yağmur duruyor" derken buna bakar.) */
    val isWet: Boolean get() = this in setOf(Drizzle, FreezingRain, Rain, Snow, RainShowers, SnowShowers, Thunderstorm)
}

/** WMO 4677 kod tablosu. Farklı bir sağlayıcının kodları için yeni implementasyon bağlanır. */
class WmoWeatherConditionClassifier @Inject constructor() : WeatherConditionClassifier {
    override fun classify(code: WeatherCode): WeatherCondition = when (code.value) {
        0 -> WeatherCondition.Clear
        1 -> WeatherCondition.MainlyClear
        2 -> WeatherCondition.PartlyCloudy
        3 -> WeatherCondition.Overcast
        45, 48 -> WeatherCondition.Fog
        51, 53, 55 -> WeatherCondition.Drizzle
        56, 57, 66, 67 -> WeatherCondition.FreezingRain
        61, 63, 65 -> WeatherCondition.Rain
        71, 73, 75, 77 -> WeatherCondition.Snow
        80, 81, 82 -> WeatherCondition.RainShowers
        85, 86 -> WeatherCondition.SnowShowers
        95, 96, 99 -> WeatherCondition.Thunderstorm
        else -> WeatherCondition.Unknown
    }
}
