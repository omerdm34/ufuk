package com.kampplus.ufuk.core.network.di

import javax.inject.Qualifier

/** Open-Meteo üç ayrı host kullanır; her biri için ayrı Retrofit örneği vardır. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ForecastRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AirQualityRetrofit
