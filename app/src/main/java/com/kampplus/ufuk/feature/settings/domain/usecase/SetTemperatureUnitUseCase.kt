package com.kampplus.ufuk.feature.settings.domain.usecase

import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class SetTemperatureUnitUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(unit: TemperatureUnit) = repository.setTemperatureUnit(unit)
}
