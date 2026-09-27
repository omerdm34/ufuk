package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import javax.inject.Inject

class RemoveSavedPlaceUseCase @Inject constructor(
    private val repository: SavedPlaceRepository
) {
    suspend operator fun invoke(placeId: Long): RemovedPlace? = repository.remove(placeId)
}
