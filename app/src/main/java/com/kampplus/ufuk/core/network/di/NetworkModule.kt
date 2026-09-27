package com.kampplus.ufuk.core.network.di

import com.kampplus.ufuk.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * HTTP önbelleği bilinçli olarak yok: Open-Meteo `Cache-Control` başlığı göndermediği için
 * OkHttp önbelleği hiçbir yanıtı saklamaz. Çevrimdışı çalışma Room önbelleğiyle sağlanır.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideConverterFactory(json: Json): Converter.Factory = json.asConverterFactory("application/json".toMediaType())

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    @ForecastRetrofit
    fun provideForecastRetrofit(client: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
        retrofit(BuildConfig.FORECAST_BASE_URL, client, converterFactory)

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(client: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
        retrofit(BuildConfig.GEOCODING_BASE_URL, client, converterFactory)

    @Provides
    @Singleton
    @AirQualityRetrofit
    fun provideAirQualityRetrofit(client: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
        retrofit(BuildConfig.AIR_QUALITY_BASE_URL, client, converterFactory)

    private fun retrofit(baseUrl: String, client: OkHttpClient, converterFactory: Converter.Factory): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
}
