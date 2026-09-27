package com.kampplus.ufuk.feature.forecast.data.local

import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import java.time.Instant
import kotlinx.coroutines.flow.Flow

data class CachedPayload(
    val payload: ForecastPayload,
    val fetchedAt: Instant
)

/** Tahmin önbelleği. Okunamayan (bozuk ya da eski biçimli) kayıt yokmuş gibi davranır. */
interface ForecastLocalDataSource {
    fun observe(cacheKey: String): Flow<CachedPayload?>

    suspend fun get(cacheKey: String): CachedPayload?

    suspend fun save(cacheKey: String, payload: ForecastPayload, fetchedAt: Instant)
}
