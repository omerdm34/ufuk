package com.kampplus.ufuk.feature.places.domain.repository

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import kotlinx.coroutines.flow.Flow

/** Kullanıcının yerleri. Cihaz konumu (varsa) her zaman ilk sıradadır, diğerleri eklenme sırasıyla. */
interface SavedPlaceRepository {
    fun observeSavedPlaces(): Flow<List<Place>>

    /** Listenin sonuna ekler; zaten kayıtlıysa yerini değiştirmez. */
    suspend fun save(place: Place)

    suspend fun remove(placeId: Long): RemovedPlace?

    /** Silinen yeri eski sırasına geri koyar. */
    suspend fun restore(removed: RemovedPlace)

    /** Cihaz konumunu (Place.DEVICE_LOCATION_ID) günceller ya da ekler. */
    suspend fun saveDeviceLocation(place: Place)
}
