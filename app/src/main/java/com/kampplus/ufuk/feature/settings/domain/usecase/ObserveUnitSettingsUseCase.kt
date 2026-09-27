package com.kampplus.ufuk.feature.settings.domain.usecase

import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Birimleri gösteren her ekran bunu izler; ayar değişince açık ekranlar da anında güncellenir. */
class ObserveUnitSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<UnitSettings> = repository.unitSettings.distinctUntilChanged()
}
