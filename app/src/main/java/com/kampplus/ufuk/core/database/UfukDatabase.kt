package com.kampplus.ufuk.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kampplus.ufuk.feature.forecast.data.local.dao.ForecastCacheDao
import com.kampplus.ufuk.feature.forecast.data.local.entity.ForecastCacheEntity

/**
 * Uygulamanın tek veritabanı. Yeni tablo eklemek için entity listesine eklenir, sürüm artırılır
 * ve bir migration (ya da şema dosyaları repoda olduğu için AutoMigration) tanımlanır.
 */
@Database(
    entities = [ForecastCacheEntity::class],
    version = 1,
    exportSchema = true
)
abstract class UfukDatabase : RoomDatabase() {
    abstract fun forecastCacheDao(): ForecastCacheDao

    companion object {
        const val NAME = "ufuk.db"
    }
}
