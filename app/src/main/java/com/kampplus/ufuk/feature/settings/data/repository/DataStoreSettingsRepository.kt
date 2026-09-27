package com.kampplus.ufuk.feature.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.feature.settings.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Ayarlar enum adıyla saklanır. Okunamayan dosya ya da tanınmayan değer (ör. ileride kaldırılmış
 * bir birim) varsayılana düşer; ayarlar yüzünden uygulama açılmaz hale gelmez.
 */
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val unitSettings: Flow<UnitSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            UnitSettings(
                temperature = preferences[TEMPERATURE_UNIT].toEnumOr(TemperatureUnit.Celsius),
                windSpeed = preferences[WIND_SPEED_UNIT].toEnumOr(WindSpeedUnit.KilometresPerHour)
            )
        }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[TEMPERATURE_UNIT] = unit.name }
    }

    override suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { it[WIND_SPEED_UNIT] = unit.name }
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T = enumValues<T>().firstOrNull { it.name == this } ?: default

    private companion object {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
    }
}
