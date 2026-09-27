package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Keşfet ve tahmin ekranlarının "kayıtlı mı?" sorusunu tek kaynaktan cevaplaması için. */
class ObserveSavedPlaceIdsUseCase @Inject constructor(
    private val repository: SavedPlaceRepository
) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeSavedPlaces()
        .map { places -> places.mapTo(mutableSetOf()) { it.id } }
        .distinctUntilChanged()
}
