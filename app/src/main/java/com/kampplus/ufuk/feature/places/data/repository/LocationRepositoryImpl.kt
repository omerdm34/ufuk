package com.kampplus.ufuk.feature.places.data.repository

import com.kampplus.ufuk.core.common.dispatcher.IoDispatcher
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.data.location.DeviceLocationDataSource
import com.kampplus.ufuk.feature.places.data.location.PlaceName
import com.kampplus.ufuk.feature.places.data.location.ReverseGeocoder
import com.kampplus.ufuk.feature.places.domain.repository.LocationRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class LocationRepositoryImpl @Inject constructor(
    private val location: DeviceLocationDataSource,
    private val reverseGeocoder: ReverseGeocoder,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocationRepository {

    override fun hasPermission(): Boolean = location.hasPermission()

    override suspend fun locateDevice(): AppResult<Place> = withContext(ioDispatcher) {
        val coordinates = try {
            location.currentCoordinates()
        } catch (e: SecurityException) {
            return@withContext AppResult.Failure(AppError.LocationPermissionDenied)
        } ?: return@withContext AppResult.Failure(AppError.LocationUnavailable)

        val name = describeOrNull(coordinates)
        AppResult.Success(
            Place(
                id = Place.DEVICE_LOCATION_ID,
                name = name?.name.orEmpty(),
                region = name?.region,
                country = name?.country,
                coordinates = coordinates
            )
        )
    }

    /** Ad bulunamaması konumu geçersiz kılmaz: hava için koordinat yeterlidir. */
    private suspend fun describeOrNull(coordinates: Coordinates): PlaceName? = try {
        reverseGeocoder.describe(coordinates)
    } catch (e: IOException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
