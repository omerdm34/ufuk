package com.kampplus.ufuk.feature.settings.domain.repository

import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val unitSettings: Flow<UnitSettings>

    suspend fun setTemperatureUnit(unit: TemperatureUnit)

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit)
}
