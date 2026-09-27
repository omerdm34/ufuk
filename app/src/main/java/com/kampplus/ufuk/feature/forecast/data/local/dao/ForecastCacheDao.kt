package com.kampplus.ufuk.feature.forecast.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kampplus.ufuk.feature.forecast.data.local.entity.ForecastCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastCacheDao {
    @Query("SELECT * FROM forecast_cache WHERE cache_key = :cacheKey")
    fun observe(cacheKey: String): Flow<ForecastCacheEntity?>

    @Query("SELECT * FROM forecast_cache WHERE cache_key = :cacheKey")
    suspend fun get(cacheKey: String): ForecastCacheEntity?

    @Upsert
    suspend fun upsert(entity: ForecastCacheEntity)

    @Query("DELETE FROM forecast_cache WHERE fetched_at < :epochMillis")
    suspend fun deleteOlderThan(epochMillis: Long)
}
