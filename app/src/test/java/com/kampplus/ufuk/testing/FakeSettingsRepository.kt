package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: UnitSettings = UnitSettings()) : SettingsRepository {
    override val unitSettings = MutableStateFlow(initial)

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = unitSettings.update { it.copy(temperature = unit) }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) = unitSettings.update { it.copy(windSpeed = unit) }
}
