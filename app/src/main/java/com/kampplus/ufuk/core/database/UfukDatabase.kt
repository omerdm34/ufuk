package com.kampplus.ufuk.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.kampplus.ufuk.feature.forecast.data.local.dao.ForecastCacheDao
import com.kampplus.ufuk.feature.forecast.data.local.entity.ForecastCacheEntity
import com.kampplus.ufuk.feature.places.data.local.dao.SavedPlaceDao
import com.kampplus.ufuk.feature.places.data.local.entity.SavedPlaceEntity

/**
 * Uygulamanın tek veritabanı. Yeni tablo eklemek için entity listesine eklenir, sürüm artırılır
 * ve bir migration (ya da şema dosyaları repoda olduğu için AutoMigration) tanımlanır.
 */
@Database(
    entities = [ForecastCacheEntity::class, SavedPlaceEntity::class],
    version = 2,
    exportSchema = true,
    // 1 → 2: saved_places tablosu eklendi. Şema dosyaları repoda olduğu için Room farkı kendisi çıkarır.
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class UfukDatabase : RoomDatabase() {
    abstract fun forecastCacheDao(): ForecastCacheDao

    abstract fun savedPlaceDao(): SavedPlaceDao

    companion object {
        const val NAME = "ufuk.db"
    }
}
