package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Kayıtlıysa çıkarır, değilse ekler. Karar veritabanındaki güncel listeye göre verilir;
 * ekrandaki (bir an önceki) duruma göre değil. Çıkarıldıysa geri almak için [RemovedPlace] döner.
 */
class ToggleSavedPlaceUseCase @Inject constructor(
    private val repository: SavedPlaceRepository
) {
    suspend operator fun invoke(place: Place): RemovedPlace? {
        val isSaved = repository.observeSavedPlaces().first().any { it.id == place.id }
        return if (isSaved) {
            repository.remove(place.id)
        } else {
            repository.save(place)
            null
        }
    }
}
