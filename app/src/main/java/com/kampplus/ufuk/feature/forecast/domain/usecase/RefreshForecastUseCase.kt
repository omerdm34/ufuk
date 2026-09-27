package com.kampplus.ufuk.feature.forecast.domain.usecase

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import javax.inject.Inject

class RefreshForecastUseCase @Inject constructor(
    private val repository: ForecastRepository
) {
    suspend operator fun invoke(place: Place): AppResult<Unit> = repository.refreshForecast(place)
}
