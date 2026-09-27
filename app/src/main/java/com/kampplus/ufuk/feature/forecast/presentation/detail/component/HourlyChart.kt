package com.kampplus.ufuk.feature.forecast.presentation.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.drawWeatherGlyph
import com.kampplus.ufuk.core.ui.component.glyphColors
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors
import com.kampplus.ufuk.feature.forecast.presentation.model.HourlyUi

/**
 * 24 saatlik şerit: saat, ikon, sıcaklık eğrisi ve yağış olasılığı çubukları tek bir çizimde.
 * Eğrinin ilk noktası canlı okumadır ve ibre rengiyle çizilir; geri kalanı kazıma rengindedir.
 * TalkBack için şerit tek bir özet cümle olarak okunur.
 */
@Composable
fun HourlyChart(hours: List<HourlyUi>, modifier: Modifier = Modifier) {
    if (hours.isEmpty()) return
    val instruments = LocalInstrumentColors.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val textMeasurer = rememberTextMeasurer()
    val resources = LocalResources.current
    val labels = hours.map { it.label.asString(resources) }
    val precipitationLabels = hours.map { hour ->
        hour.precipitationProbability?.let { UiText.Resource(R.string.percent_value, it).asString(resources) }
    }
    val glyphColors = glyphColors(background = colors.background)
    val description = hours.take(A11Y_HOURS).mapIndexed { index, hour ->
        val precipitation = precipitationLabels[index]
        if (precipitation != null && (hour.precipitationProbability ?: 0) >= VISIBLE_PRECIPITATION) {
            resources.getString(R.string.a11y_hour_precipitation, labels[index], hour.temperatureText, precipitation)
        } else {
            resources.getString(R.string.a11y_hour, labels[index], hour.temperatureText)
        }
    }.joinToString(". ")

    val low = hours.minOf { it.temperature }
    val high = hours.maxOf { it.temperature }

    Box(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Canvas(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(COLUMN_WIDTH * hours.size)
                .height(CHART_HEIGHT)
        ) {
            val column = COLUMN_WIDTH.toPx()
            val curveTop = 74.dp.toPx()
            val curveBottom = 116.dp.toPx()
            val barTop = 130.dp.toPx()
            val barMax = 26.dp.toPx()

            fun x(index: Int) = column * index + column / 2f
            fun y(value: Float) = if (high ==
                low
            ) {
                (curveTop + curveBottom) / 2f
            } else {
                curveBottom - (value - low) / (high - low) * (curveBottom - curveTop)
            }

            val curve = Path()
            hours.forEachIndexed { index, hour ->
                if (index == 0) curve.moveTo(x(index), y(hour.temperature)) else curve.lineTo(x(index), y(hour.temperature))
            }
            drawPath(curve, color = colors.onSurfaceVariant, style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))

            hours.forEachIndexed { index, hour ->
                val cx = x(index)
                val timeStyle = typography.labelMedium.copy(color = if (hour.isNow) instruments.needle else colors.onSurfaceVariant)
                val time = textMeasurer.measure(labels[index], timeStyle)
                drawText(time, topLeft = Offset(cx - time.size.width / 2f, 0f))

                val glyphSide = 24.dp.toPx()
                drawWeatherGlyph(hour.glyph, hour.isDay, glyphColors, topLeft = Offset(cx - glyphSide / 2f, 22.dp.toPx()), side = glyphSide)

                val point = Offset(cx, y(hour.temperature))
                val temperature = textMeasurer.measure(hour.temperatureText, typography.labelLarge.copy(color = colors.onSurface))
                drawText(temperature, topLeft = Offset(cx - temperature.size.width / 2f, point.y - temperature.size.height - 6.dp.toPx()))
                if (hour.isNow) {
                    drawCircle(instruments.needle, radius = 4.5.dp.toPx(), center = point)
                } else {
                    drawCircle(colors.background, radius = 3.dp.toPx(), center = point)
                    drawCircle(colors.onSurfaceVariant, radius = 3.dp.toPx(), center = point, style = Stroke(1.5.dp.toPx()))
                }

                val probability = hour.precipitationProbability ?: 0
                drawRoundRect(
                    color = instruments.track,
                    topLeft = Offset(cx - BAR_HALF_WIDTH.toPx(), barTop),
                    size = Size(BAR_HALF_WIDTH.toPx() * 2, barMax),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                if (probability > 0) {
                    val height = barMax * probability / 100f
                    drawRoundRect(
                        color = instruments.rain,
                        topLeft = Offset(cx - BAR_HALF_WIDTH.toPx(), barTop + barMax - height),
                        size = Size(BAR_HALF_WIDTH.toPx() * 2, height),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
                val precipitation = precipitationLabels[index]
                if (precipitation != null && probability >= VISIBLE_PRECIPITATION) {
                    val label = textMeasurer.measure(precipitation, typography.labelSmall.copy(color = instruments.rain))
                    drawText(label, topLeft = Offset(cx - label.size.width / 2f, barTop + barMax + 4.dp.toPx()))
                }
            }
        }
    }
}

private val COLUMN_WIDTH = 56.dp
private val CHART_HEIGHT = 176.dp
private val BAR_HALF_WIDTH = 9.dp
private const val VISIBLE_PRECIPITATION = 20
private const val A11Y_HOURS = 12
