package com.kampplus.ufuk.feature.places.data.di

import com.kampplus.ufuk.core.network.di.GeocodingRetrofit
import com.kampplus.ufuk.feature.places.data.catalog.FeaturedPlaceCatalog
import com.kampplus.ufuk.feature.places.data.catalog.TurkishCityCatalog
import com.kampplus.ufuk.feature.places.data.location.AndroidDeviceLocationDataSource
import com.kampplus.ufuk.feature.places.data.location.AndroidReverseGeocoder
import com.kampplus.ufuk.feature.places.data.location.DeviceLocationDataSource
import com.kampplus.ufuk.feature.places.data.location.ReverseGeocoder
import com.kampplus.ufuk.feature.places.data.remote.OpenMeteoPlaceRemoteDataSource
import com.kampplus.ufuk.feature.places.data.remote.PlaceRemoteDataSource
import com.kampplus.ufuk.feature.places.data.remote.api.OpenMeteoGeocodingApi
import com.kampplus.ufuk.feature.places.data.repository.LocationRepositoryImpl
import com.kampplus.ufuk.feature.places.data.repository.PlaceSearchRepositoryImpl
import com.kampplus.ufuk.feature.places.data.repository.RoomSavedPlaceRepository
import com.kampplus.ufuk.feature.places.domain.repository.LocationRepository
import com.kampplus.ufuk.feature.places.domain.repository.PlaceSearchRepository
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
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
abstract class PlacesDataModule {
    @Binds
    @Singleton
    abstract fun bindSavedPlaceRepository(impl: RoomSavedPlaceRepository): SavedPlaceRepository

    @Binds
    abstract fun bindPlaceSearchRepository(impl: PlaceSearchRepositoryImpl): PlaceSearchRepository

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindPlaceRemoteDataSource(impl: OpenMeteoPlaceRemoteDataSource): PlaceRemoteDataSource

    @Binds
    abstract fun bindFeaturedPlaceCatalog(impl: TurkishCityCatalog): FeaturedPlaceCatalog

    @Binds
    abstract fun bindDeviceLocationDataSource(impl: AndroidDeviceLocationDataSource): DeviceLocationDataSource

    @Binds
    abstract fun bindReverseGeocoder(impl: AndroidReverseGeocoder): ReverseGeocoder

    companion object {
        @Provides
        @Singleton
        fun provideGeocodingApi(@GeocodingRetrofit retrofit: Retrofit): OpenMeteoGeocodingApi =
            retrofit.create(OpenMeteoGeocodingApi::class.java)
    }
}
