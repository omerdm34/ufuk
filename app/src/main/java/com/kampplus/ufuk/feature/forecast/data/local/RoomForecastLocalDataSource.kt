package com.kampplus.ufuk.feature.forecast.data.local

import com.kampplus.ufuk.feature.forecast.data.local.dao.ForecastCacheDao
import com.kampplus.ufuk.feature.forecast.data.local.entity.ForecastCacheEntity
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class RoomForecastLocalDataSource @Inject constructor(
    private val dao: ForecastCacheDao,
    private val json: Json
) : ForecastLocalDataSource {

    override fun observe(cacheKey: String): Flow<CachedPayload?> = dao.observe(cacheKey)
        .distinctUntilChanged()
        .map { it?.toCached() }

    override suspend fun get(cacheKey: String): CachedPayload? = dao.get(cacheKey)?.toCached()

    /** Yazarken bir haftadan eski kayıtlar silinir; önbellek ziyaret edilen yer sayısıyla sınırlı kalır. */
    override suspend fun save(cacheKey: String, payload: ForecastPayload, fetchedAt: Instant) {
        dao.upsert(
            ForecastCacheEntity(
                cacheKey = cacheKey,
                payloadJson = json.encodeToString(ForecastPayload.serializer(), payload),
                fetchedAtEpochMillis = fetchedAt.toEpochMilli()
            )
        )
        dao.deleteOlderThan(fetchedAt.minus(RETENTION).toEpochMilli())
    }

    private fun ForecastCacheEntity.toCached(): CachedPayload? = try {
        CachedPayload(
            payload = json.decodeFromString(ForecastPayload.serializer(), payloadJson),
            fetchedAt = Instant.ofEpochMilli(fetchedAtEpochMillis)
        )
    } catch (e: SerializationException) {
        null
    }

    private companion object {
        val RETENTION: Duration = Duration.ofDays(7)
    }
}
