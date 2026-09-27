package com.kampplus.ufuk.feature.places

import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.network.di.NetworkModule
import com.kampplus.ufuk.feature.places.data.location.DeviceLocationDataSource
import com.kampplus.ufuk.feature.places.data.location.PlaceName
import com.kampplus.ufuk.feature.places.data.remote.OpenMeteoPlaceRemoteDataSource
import com.kampplus.ufuk.feature.places.data.remote.api.OpenMeteoGeocodingApi
import com.kampplus.ufuk.feature.places.data.repository.LocationRepositoryImpl
import com.kampplus.ufuk.feature.places.domain.usecase.LocateDeviceUseCase
import com.kampplus.ufuk.testing.FakeSavedPlaceRepository
import com.kampplus.ufuk.testing.readResource
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit

class PlacesDataTest {

    @Test
    fun `search keeps populated places and drops airports and peaks`() = runTest {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setBody(readResource("geocoding_ankara.json")))
            start()
        }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/v1/"))
            .addConverterFactory(NetworkModule.provideConverterFactory(NetworkModule.provideJson()))
            .build()
            .create(OpenMeteoGeocodingApi::class.java)

        val places = OpenMeteoPlaceRemoteDataSource(api).search("Ankara", "tr")

        assertEquals(listOf("Ankara", "Ankara Çayı Köyü"), places.map { it.name })
        assertEquals("tr", server.takeRequest().requestUrl!!.queryParameter("language"))
        server.shutdown()
    }

    @Test
    fun `location without a name is still a place`() = runTest {
        val repository = LocationRepositoryImpl(
            location = FakeDevice(Coordinates(39.91, 32.86)),
            reverseGeocoder = { throw IOException("no backend") },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        val place = (repository.locateDevice() as AppResult.Success).data

        assertEquals(Place.DEVICE_LOCATION_ID, place.id)
        assertEquals("", place.name)
    }

    @Test
    fun `missing permission and missing fix are different errors`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val denied = LocationRepositoryImpl(FakeDevice(null, denied = true), { null }, dispatcher)
        val noFix = LocationRepositoryImpl(FakeDevice(null), { null }, dispatcher)

        assertEquals(AppResult.Failure(AppError.LocationPermissionDenied), denied.locateDevice())
        assertEquals(AppResult.Failure(AppError.LocationUnavailable), noFix.locateDevice())
    }

    @Test
    fun `located device is stored first under its reverse geocoded name`() = runTest {
        val saved = FakeSavedPlaceRepository()
        val repository = LocationRepositoryImpl(
            location = FakeDevice(Coordinates(39.91, 32.86)),
            reverseGeocoder = { PlaceName("Çankaya", "Ankara", "Türkiye") },
            ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        LocateDeviceUseCase(repository, saved)()

        assertEquals("Çankaya", saved.observeSavedPlaces().first().single().name)
    }

    private class FakeDevice(private val coordinates: Coordinates?, private val denied: Boolean = false) : DeviceLocationDataSource {
        override fun hasPermission() = !denied

        override suspend fun currentCoordinates(): Coordinates? {
            if (denied) throw SecurityException()
            return coordinates
        }
    }
}
