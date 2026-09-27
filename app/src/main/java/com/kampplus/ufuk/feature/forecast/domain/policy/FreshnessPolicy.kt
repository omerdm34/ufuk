package com.kampplus.ufuk.feature.forecast.domain.policy

import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Önbellekteki tahminin ne zaman yenileneceğine ve ne zaman "eski" sayılacağına karar verir.
 * Open-Meteo modelleri saatlik güncellenir; 10 dakikadan sık istek atmak bilgi getirmez.
 */
class FreshnessPolicy @Inject constructor() {
    fun shouldRefresh(fetchedAt: Instant?, now: Instant): Boolean = fetchedAt == null || age(fetchedAt, now) >= REFRESH_AFTER

    /** Eski veri, ekranda yaşıyla ve soluk ibreyle gösterilir; asla canlıymış gibi sunulmaz. */
    fun isStale(fetchedAt: Instant, now: Instant): Boolean = age(fetchedAt, now) >= STALE_AFTER

    fun age(fetchedAt: Instant, now: Instant): Duration = Duration.between(fetchedAt, now).let { if (it.isNegative) Duration.ZERO else it }

    companion object {
        val REFRESH_AFTER: Duration = Duration.ofMinutes(10)
        val STALE_AFTER: Duration = Duration.ofMinutes(45)
    }
}
