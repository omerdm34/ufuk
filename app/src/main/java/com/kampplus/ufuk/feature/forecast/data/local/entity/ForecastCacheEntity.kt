package com.kampplus.ufuk.feature.forecast.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Bir konumun son tahmini, API yanıtının JSON hali olarak. Anahtar: Coordinates.cacheKey. */
@Entity(tableName = "forecast_cache")
data class ForecastCacheEntity(
    @PrimaryKey @ColumnInfo(name = "cache_key") val cacheKey: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    @ColumnInfo(name = "fetched_at") val fetchedAtEpochMillis: Long
)
