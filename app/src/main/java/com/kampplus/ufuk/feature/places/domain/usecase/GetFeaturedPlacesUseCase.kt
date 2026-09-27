package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.repository.PlaceSearchRepository
import javax.inject.Inject

class GetFeaturedPlacesUseCase @Inject constructor(
    private val repository: PlaceSearchRepository
) {
    operator fun invoke(): List<Place> = repository.featured()
}
