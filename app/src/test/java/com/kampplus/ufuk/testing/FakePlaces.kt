package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import com.kampplus.ufuk.feature.places.domain.repository.LocationRepository
import com.kampplus.ufuk.feature.places.domain.repository.PlaceSearchRepository
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/** Room davranışını (cihaz konumu başta, diğerleri sırayla, geri almada eski sıra) bellekte taklit eder. */
class FakeSavedPlaceRepository(initial: List<Place> = emptyList()) : SavedPlaceRepository {
    private val device = MutableStateFlow<Place?>(null)
    private val saved = MutableStateFlow(initial)

    override fun observeSavedPlaces(): Flow<List<Place>> = combine(device, saved) { device, saved ->
        listOfNotNull(device) + saved
    }

    override suspend fun save(place: Place) = saved.update { current ->
        if (current.any { it.id == place.id }) current else current + place
    }

    override suspend fun remove(placeId: Long): RemovedPlace? {
        if (placeId == Place.DEVICE_LOCATION_ID) {
            val place = device.value ?: return null
            device.value = null
            return RemovedPlace(place, -1)
        }
        val index = saved.value.indexOfFirst { it.id == placeId }.takeIf { it >= 0 } ?: return null
        val place = saved.value[index]
        saved.update { it - place }
        return RemovedPlace(place, index)
    }

    override suspend fun restore(removed: RemovedPlace) = saved.update { current ->
        current.toMutableList().apply { add(removed.position.coerceIn(0, size), removed.place) }
    }

    override suspend fun saveDeviceLocation(place: Place) {
        device.value = place
    }

    fun savedNames(): List<String> = saved.value.map { it.name }
}

class FakePlaceSearchRepository(
    private val featured: List<Place> = listOf(istanbul, ankara, izmir),
    var searchResult: (String) -> AppResult<List<Place>> = { AppResult.Success(emptyList()) }
) : PlaceSearchRepository {
    val queries = mutableListOf<String>()

    override suspend fun search(query: String): AppResult<List<Place>> {
        queries += query
        return searchResult(query)
    }

    override fun featured(): List<Place> = featured
}

class FakeLocationRepository(
    var result: AppResult<Place> = AppResult.Failure(AppError.LocationUnavailable),
    var permission: Boolean = true
) : LocationRepository {
    override fun hasPermission(): Boolean = permission

    override suspend fun locateDevice(): AppResult<Place> = result
}
