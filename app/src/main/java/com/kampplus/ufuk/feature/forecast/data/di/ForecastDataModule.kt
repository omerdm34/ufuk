package com.kampplus.ufuk.feature.forecast.data.di

import com.kampplus.ufuk.core.network.di.AirQualityRetrofit
import com.kampplus.ufuk.core.network.di.ForecastRetrofit
import com.kampplus.ufuk.feature.forecast.data.local.ForecastLocalDataSource
import com.kampplus.ufuk.feature.forecast.data.local.RoomForecastLocalDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.ForecastRemoteDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.OpenMeteoForecastRemoteDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoAirQualityApi
import com.kampplus.ufuk.feature.forecast.data.remote.api.OpenMeteoForecastApi
import com.kampplus.ufuk.feature.forecast.data.repository.ForecastRepositoryImpl
import com.kampplus.ufuk.feature.forecast.domain.policy.WeatherConditionClassifier
import com.kampplus.ufuk.feature.forecast.domain.policy.WmoWeatherConditionClassifier
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/** Composition root: hangi implementasyonun kullanılacağına yalnızca burada karar verilir. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ForecastDataModule {
    @Binds
    @Singleton
    abstract fun bindForecastRepository(impl: ForecastRepositoryImpl): ForecastRepository

    @Binds
    abstract fun bindForecastRemoteDataSource(impl: OpenMeteoForecastRemoteDataSource): ForecastRemoteDataSource

    @Binds
    abstract fun bindForecastLocalDataSource(impl: RoomForecastLocalDataSource): ForecastLocalDataSource

    @Binds
    abstract fun bindWeatherConditionClassifier(impl: WmoWeatherConditionClassifier): WeatherConditionClassifier

    companion object {
        @Provides
        @Singleton
        fun provideForecastApi(@ForecastRetrofit retrofit: Retrofit): OpenMeteoForecastApi =
            retrofit.create(OpenMeteoForecastApi::class.java)

        @Provides
        @Singleton
        fun provideAirQualityApi(@AirQualityRetrofit retrofit: Retrofit): OpenMeteoAirQualityApi =
            retrofit.create(OpenMeteoAirQualityApi::class.java)
    }
}
