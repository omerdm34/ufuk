package com.kampplus.ufuk.core.database.di

import android.content.Context
import androidx.room.Room
import com.kampplus.ufuk.core.database.UfukDatabase
import com.kampplus.ufuk.feature.forecast.data.local.dao.ForecastCacheDao
import com.kampplus.ufuk.feature.places.data.local.dao.SavedPlaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UfukDatabase =
        Room.databaseBuilder(context, UfukDatabase::class.java, UfukDatabase.NAME).build()

    @Provides
    fun provideForecastCacheDao(database: UfukDatabase): ForecastCacheDao = database.forecastCacheDao()

    @Provides
    fun provideSavedPlaceDao(database: UfukDatabase): SavedPlaceDao = database.savedPlaceDao()
}
