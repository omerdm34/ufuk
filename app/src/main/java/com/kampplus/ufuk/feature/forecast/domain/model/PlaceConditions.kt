package com.kampplus.ufuk.feature.forecast.domain.model

import com.kampplus.ufuk.core.model.Place
import java.time.Instant
import java.time.LocalDateTime

/** Listelerde bir yerin satırı: şu anki durum ve bugünün aralığı. */
data class PlaceConditions(
    val place: Place,
    val temperatureC: Double,
    val weatherCode: WeatherCode,
    val isDay: Boolean,
    val todayMinC: Double?,
    val todayMaxC: Double?,
    val localTime: LocalDateTime,
    val fetchedAt: Instant,
    /** Ağ yokken önbellekteki son tahminden türetildiyse true; ekran bunu yaşıyla birlikte söyler. */
    val isFromCache: Boolean = false
)
