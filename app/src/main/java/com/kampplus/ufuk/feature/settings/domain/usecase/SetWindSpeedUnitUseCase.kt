package com.kampplus.ufuk.feature.settings.domain.usecase

import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class SetWindSpeedUnitUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(unit: WindSpeedUnit) = repository.setWindSpeedUnit(unit)
}
