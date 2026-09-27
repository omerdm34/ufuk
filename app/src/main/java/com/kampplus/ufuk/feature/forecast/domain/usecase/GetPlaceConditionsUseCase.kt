package com.kampplus.ufuk.feature.forecast.domain.usecase

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import javax.inject.Inject

class GetPlaceConditionsUseCase @Inject constructor(
    private val repository: ForecastRepository
) {
    suspend operator fun invoke(places: List<Place>): AppResult<List<PlaceConditions>> =
        if (places.isEmpty()) AppResult.Success(emptyList()) else repository.getConditions(places)
}
