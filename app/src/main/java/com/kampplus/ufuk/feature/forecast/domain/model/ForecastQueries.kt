package com.kampplus.ufuk.feature.forecast.domain.model

import java.time.temporal.ChronoUnit

// Tahmin üzerinde sık sorulan sorular. Hepsi "şimdi"yi yerin yerel saatinden (CurrentConditions.time) alır.

fun Forecast.today(): DailyForecast? = daily.firstOrNull { it.date == current.time.toLocalDate() }

fun Forecast.tomorrow(): DailyForecast? = daily.firstOrNull { it.date == current.time.toLocalDate().plusDays(1) }

/** Bugünden başlayan günler (dün hariç). */
fun Forecast.upcomingDays(): List<DailyForecast> = daily.filter { !it.date.isBefore(current.time.toLocalDate()) }

/** İçinde bulunulan saatten başlayan [count] saat. */
fun Forecast.upcomingHours(count: Int): List<HourlyForecast> {
    val currentHour = current.time.truncatedTo(ChronoUnit.HOURS)
    return hourly.filter { !it.time.isBefore(currentHour) }.take(count)
}

/** [hours] saat önceki ölçüm; yoksa null. */
fun Forecast.hoursAgo(hours: Long): HourlyForecast? {
    val target = current.time.truncatedTo(ChronoUnit.HOURS).minusHours(hours)
    return hourly.firstOrNull { it.time == target }
}

/** Dün aynı saatteki ölçüm (ayar ibresinin gösterdiği değer). */
fun Forecast.sameHourYesterday(): HourlyForecast? = hoursAgo(HOURS_PER_DAY)

private const val HOURS_PER_DAY = 24L
