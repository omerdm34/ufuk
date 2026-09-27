package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import java.time.Instant
import java.time.LocalDateTime

/** Her yer için ada göre deterministik bir sıcaklık üretir: İstanbul 19, Ankara 22, İzmir 26. */
fun conditionsFor(place: Place, fromCache: Boolean = false) = PlaceConditions(
    place = place,
    temperatureC = when (place.name) {
        "İstanbul" -> 19.4
        "Ankara" -> 22.4
        "İzmir" -> 26.1
        else -> 15.0
    },
    weatherCode = WeatherCode(1),
    isDay = true,
    todayMinC = 12.0,
    todayMaxC = 27.0,
    localTime = LocalDateTime.of(2026, 9, 27, 14, 15),
    fetchedAt = Instant.parse("2026-09-27T11:15:00Z"),
    isFromCache = fromCache
)
