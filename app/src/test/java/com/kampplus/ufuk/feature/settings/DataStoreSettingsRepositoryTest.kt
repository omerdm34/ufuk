package com.kampplus.ufuk.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.feature.settings.data.repository.DataStoreSettingsRepository
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(scope = backgroundScope) {
        File(folder.root, "settings.preferences_pb")
    }

    @Test
    fun `defaults are metric`() = runTest {
        val repository = DataStoreSettingsRepository(dataStore())

        assertEquals(UnitSettings(), repository.unitSettings.first())
    }

    @Test
    fun `chosen units are persisted`() = runTest {
        val repository = DataStoreSettingsRepository(dataStore())

        repository.setTemperatureUnit(TemperatureUnit.Fahrenheit)
        repository.setWindSpeedUnit(WindSpeedUnit.MetresPerSecond)

        assertEquals(UnitSettings(TemperatureUnit.Fahrenheit, WindSpeedUnit.MetresPerSecond), repository.unitSettings.first())
    }

    @Test
    fun `unknown stored value falls back to the default`() = runTest {
        val store = dataStore()
        store.edit { it[stringPreferencesKey("temperature_unit")] = "Kelvin" }

        assertEquals(TemperatureUnit.Celsius, DataStoreSettingsRepository(store).unitSettings.first().temperature)
    }
}
