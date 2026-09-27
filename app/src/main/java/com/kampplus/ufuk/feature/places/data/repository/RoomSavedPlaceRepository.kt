package com.kampplus.ufuk.feature.places.data.repository

import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.data.local.dao.SavedPlaceDao
import com.kampplus.ufuk.feature.places.data.local.entity.SavedPlaceEntity
import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSavedPlaceRepository @Inject constructor(
    private val dao: SavedPlaceDao,
    private val clock: Clock
) : SavedPlaceRepository {

    override fun observeSavedPlaces(): Flow<List<Place>> = dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun save(place: Place) = dao.append(place.toEntity(position = 0))

    override suspend fun remove(placeId: Long): RemovedPlace? =
        dao.remove(placeId)?.let { RemovedPlace(place = it.toDomain(), position = it.position) }

    override suspend fun restore(removed: RemovedPlace) = dao.insertAt(removed.place.toEntity(position = removed.position))

    override suspend fun saveDeviceLocation(place: Place) =
        dao.upsert(place.copy(id = Place.DEVICE_LOCATION_ID).toEntity(position = DEVICE_POSITION))

    private fun SavedPlaceEntity.toDomain() = Place(
        id = id,
        name = name,
        region = region,
        country = country,
        coordinates = Coordinates(latitude = latitude, longitude = longitude)
    )

    private fun Place.toEntity(position: Int) = SavedPlaceEntity(
        id = id,
        name = name,
        region = region,
        country = country,
        latitude = coordinates.latitude,
        longitude = coordinates.longitude,
        position = position,
        addedAtEpochMillis = clock.millis()
    )

    private companion object {
        const val DEVICE_POSITION = -1
    }
}
