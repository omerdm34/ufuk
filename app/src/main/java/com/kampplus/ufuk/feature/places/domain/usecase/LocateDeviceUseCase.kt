package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.repository.LocationRepository
import com.kampplus.ufuk.feature.places.domain.repository.SavedPlaceRepository
import javax.inject.Inject

/** Konumu bulur ve "Konumum" olarak yerlerin başına yazar; başarısızsa eski konum yerinde kalır. */
class LocateDeviceUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val savedPlaceRepository: SavedPlaceRepository
) {
    suspend operator fun invoke(): AppResult<Place> {
        val result = locationRepository.locateDevice()
        if (result is AppResult.Success) savedPlaceRepository.saveDeviceLocation(result.data)
        return result
    }
}
