package com.kampplus.ufuk.feature.forecast.domain.usecase

import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.domain.model.ForecastSnapshot
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveForecastUseCase @Inject constructor(
    private val repository: ForecastRepository
) {
    operator fun invoke(place: Place): Flow<ForecastSnapshot?> = repository.observeForecast(place)
}
