package com.kampplus.ufuk.feature.forecast.presentation.model

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.format.UnitFormatter
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** Listelerde bir yerin satırı. Hava verisi yoksa (çevrimdışı, önbellek boş) yalnızca ad görünür. */
data class PlaceRowUi(
    val placeId: Long,
    val title: UiText,
    val region: String?,
    val detail: UiText,
    val temperatureText: String?,
    val temperature: Float?,
    val todayMin: Float?,
    val todayMax: Float?,
    val rangeText: UiText?,
    val glyph: GlyphKind,
    val isDay: Boolean,
    val isDeviceLocation: Boolean,
    val isSaved: Boolean,
    val description: UiText
)

class PlaceRowMapper @Inject constructor(
    private val conditions: ConditionUiRegistry,
    private val forecastUiMapper: ForecastUiMapper
) {
    fun toRow(
        place: Place,
        weather: PlaceConditions?,
        units: UnitSettings,
        isSaved: Boolean = false,
        showRegion: Boolean = false
    ): PlaceRowUi {
        val format = UnitFormatter(units)
        val title = forecastUiMapper.placeTitle(place)
        val condition = weather?.let { conditions.resolve(it.weatherCode) }
        val temperatureText = weather?.let { format.temperature(it.temperatureC) }
        val rangeText = if (weather?.todayMaxC != null && weather.todayMinC != null) {
            UiText.Resource(R.string.row_range, format.temperature(weather.todayMaxC), format.temperature(weather.todayMinC))
        } else {
            null
        }
        return PlaceRowUi(
            placeId = place.id,
            title = title,
            region = if (showRegion) {
                listOfNotNull(place.region, place.country).filter {
                    it != place.name
                }.distinct().joinToString(", ")
            } else {
                null
            },
            detail = if (weather != null && condition != null) {
                UiText.Resource(R.string.row_detail, condition.label, weather.localTime.format(TIME_FORMAT))
            } else {
                UiText.Resource(R.string.row_no_data)
            },
            temperatureText = temperatureText,
            temperature = weather?.let { units.temperature.fromCelsius(it.temperatureC).toFloat() },
            todayMin = weather?.todayMinC?.let { units.temperature.fromCelsius(it).toFloat() },
            todayMax = weather?.todayMaxC?.let { units.temperature.fromCelsius(it).toFloat() },
            rangeText = rangeText,
            glyph = condition?.glyph ?: GlyphKind.Unknown,
            isDay = weather?.isDay ?: true,
            isDeviceLocation = place.isDeviceLocation,
            isSaved = isSaved,
            description = when {
                condition == null || temperatureText == null -> UiText.Resource(R.string.a11y_row_no_data, title)
                rangeText == null -> UiText.Resource(R.string.a11y_row, title, temperatureText, condition.label)
                else -> UiText.Resource(R.string.a11y_row_range, title, temperatureText, condition.label, rangeText)
            }
        )
    }

    fun title(place: Place): UiText = forecastUiMapper.placeTitle(place)

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
