package com.kampplus.ufuk.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.SetTemperatureUnitUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.SetWindSpeedUnitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeUnitSettings: ObserveUnitSettingsUseCase,
    private val setTemperatureUnit: SetTemperatureUnitUseCase,
    private val setWindSpeedUnit: SetWindSpeedUnitUseCase
) : ViewModel() {

    val units: StateFlow<UnitSettings> = observeUnitSettings().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = UnitSettings()
    )

    fun onTemperatureUnitSelected(unit: TemperatureUnit) {
        viewModelScope.launch { setTemperatureUnit(unit) }
    }

    fun onWindSpeedUnitSelected(unit: WindSpeedUnit) {
        viewModelScope.launch { setWindSpeedUnit(unit) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
