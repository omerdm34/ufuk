package com.kampplus.ufuk.feature.forecast.presentation.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.core.ui.component.WeatherGlyph
import com.kampplus.ufuk.core.ui.component.degreesText
import com.kampplus.ufuk.core.ui.component.glyphColors
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors
import com.kampplus.ufuk.feature.forecast.presentation.model.DailyRangeUi
import com.kampplus.ufuk.feature.forecast.presentation.model.DayUi

/**
 * Günlük aralık çubukları. Tüm günler aynı ölçeği paylaşır; böylece hangi günün daha sıcak ya da
 * daha geniş aralıklı olduğu sayı okumadan görülür. Bugünün satırında canlı okuma işaretlidir.
 */
@Composable
fun DailyRangeList(daily: DailyRangeUi, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        daily.days.forEachIndexed { index, day ->
            if (index >
                0
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            }
            DayRow(day = day, scaleMin = daily.scaleMin, scaleMax = daily.scaleMax, now = if (day.isToday) daily.now else null)
        }
    }
}

@Composable
private fun DayRow(day: DayUi, scaleMin: Float, scaleMax: Float, now: Float?) {
    val instruments = LocalInstrumentColors.current
    val description = day.description.asString()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.label.asString(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(76.dp)
        )
        WeatherGlyph(kind = day.glyph, isDay = true, size = 24.dp, colors = glyphColors(background = MaterialTheme.colorScheme.background))
        Text(
            text = day.precipitation?.asString().orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = instruments.rain,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp)
        )
        Text(
            text = degreesText(day.minText),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = TEMPERATURE_WIDTH)
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .padding(horizontal = 10.dp)
        ) {
            val span = (scaleMax - scaleMin).takeIf { it > 0f } ?: 1f
            fun x(value: Float) = (value - scaleMin) / span * size.width
            val y = size.height / 2f
            val stroke = 5.dp.toPx()
            drawLine(instruments.track, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(instruments.gilt, Offset(x(day.min), y), Offset(x(day.max), y), strokeWidth = stroke, cap = StrokeCap.Round)
            if (now != null) {
                drawCircle(instruments.face, radius = 5.dp.toPx(), center = Offset(x(now.coerceIn(scaleMin, scaleMax)), y))
                drawCircle(instruments.needle, radius = 3.5.dp.toPx(), center = Offset(x(now.coerceIn(scaleMin, scaleMax)), y))
            }
        }
        Text(
            text = degreesText(day.maxText),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = TEMPERATURE_WIDTH)
        )
    }
}

/** "-12°" gibi iki basamaklı eksi değerlere yeter; büyük yazı boyutunda sütun kesilmek yerine genişler. */
private val TEMPERATURE_WIDTH = 44.dp
