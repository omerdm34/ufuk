package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveSavedPlacesUseCase @Inject constructor(
    private val repository: SavedPlaceRepository
) {
    operator fun invoke(): Flow<List<Place>> = repository.observeSavedPlaces()
}
