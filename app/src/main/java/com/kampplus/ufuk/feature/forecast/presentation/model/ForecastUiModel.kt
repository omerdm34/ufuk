package com.kampplus.ufuk.feature.forecast.presentation.model

import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.text.UiText

/** Detay ekranında gösterilmeye hazır tahmin. Sayısal değerler kullanıcının birimine çevrilmiştir. */
data class ForecastUiModel(
    val title: UiText,
    val subtitle: String,
    val dial: TemperatureDialUi,
    val condition: UiText,
    val glyph: GlyphKind,
    val isDay: Boolean,
    val highLow: UiText?,
    val insights: List<UiText>,
    val readings: List<ReadingUi>,
    val hourly: List<HourlyUi>,
    val daily: DailyRangeUi,
    val instruments: InstrumentsUi,
    val shareText: UiText
)

/**
 * Ana kadran. Ölçek [scaleMin]..[scaleMax] arasındadır; yaldız yay bugünün aralığını,
 * canlı ibre [now]'ı, pirinç ayar ibresi dün aynı saati ([yesterday]) gösterir.
 */
data class TemperatureDialUi(
    val now: Float,
    val nowText: String,
    val scaleMin: Float,
    val scaleMax: Float,
    val majorStep: Int,
    val todayMin: Float?,
    val todayMax: Float?,
    val yesterday: Float?,
    val yesterdayLegend: UiText?,
    val description: UiText
)

data class ReadingUi(
    val label: UiText,
    val value: UiText
)

data class HourlyUi(
    val label: UiText,
    val isNow: Boolean,
    val temperature: Float,
    val temperatureText: String,
    val glyph: GlyphKind,
    val isDay: Boolean,
    val precipitationProbability: Int?
)

data class DailyRangeUi(
    val days: List<DayUi>,
    val scaleMin: Float,
    val scaleMax: Float,
    /** Bugünün satırında canlı okumanın konumu. */
    val now: Float?
)

data class DayUi(
    val label: UiText,
    val isToday: Boolean,
    val glyph: GlyphKind,
    val min: Float,
    val max: Float,
    val minText: String,
    val maxText: String,
    val precipitation: UiText?,
    val description: UiText
)

data class InstrumentsUi(
    val wind: GaugeUi?,
    val humidity: GaugeUi?,
    val pressure: GaugeUi?,
    val uv: GaugeUi?,
    val sun: SunUi?,
    val airQuality: GaugeUi?
)

/**
 * Küçük alet kadranı. [value] ölçek içindeki okuma, [previous] varsa ayar ibresi.
 * [direction] yalnızca pusulada kullanılır (rüzgârın estiği yön, derece).
 */
data class GaugeUi(
    val label: UiText,
    val valueText: UiText,
    val note: UiText?,
    val value: Float,
    val scaleMin: Float,
    val scaleMax: Float,
    val previous: Float? = null,
    val direction: Float? = null
)

/** Gün ışığı yayı: 0 = doğuş, 1 = batış; gece ise [progress] null. */
data class SunUi(
    val label: UiText,
    val valueText: UiText,
    val note: UiText,
    val progress: Float?
)
