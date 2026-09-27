package com.kampplus.ufuk.feature.forecast.presentation.detail.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.component.dial.DialArc
import com.kampplus.ufuk.core.ui.component.dial.dialAngle
import com.kampplus.ufuk.core.ui.component.dial.drawNeedle
import com.kampplus.ufuk.core.ui.component.dial.drawSetHand
import com.kampplus.ufuk.core.ui.component.dial.drawTicks
import com.kampplus.ufuk.core.ui.component.dial.pointOnCircle
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.theme.InstrumentColors
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors
import com.kampplus.ufuk.feature.forecast.presentation.model.GaugeUi
import com.kampplus.ufuk.feature.forecast.presentation.model.InstrumentsUi
import com.kampplus.ufuk.feature.forecast.presentation.model.SunUi
import kotlin.math.roundToInt

/**
 * Masa üstü hava istasyonunun alet takımı: her ölçüm kendi kadranında. Kutular yok; aletler
 * iki sütunlu bir levhaya dizilir. Her alet TalkBack'te tek cümle olarak okunur.
 */
@Composable
fun InstrumentPanel(instruments: InstrumentsUi, modifier: Modifier = Modifier) {
    val tiles: List<@Composable (Modifier) -> Unit> = listOfNotNull(
        instruments.wind?.let { gauge ->
            { m: Modifier -> InstrumentTile(gauge.label, gauge.valueText, gauge.note, m) { CompassFace(gauge) } }
        },
        instruments.humidity?.let { gauge ->
            { m: Modifier -> InstrumentTile(gauge.label, gauge.valueText, gauge.note, m) { ArcFace(gauge) } }
        },
        instruments.pressure?.let { gauge ->
            { m: Modifier -> InstrumentTile(gauge.label, gauge.valueText, gauge.note, m) { ArcFace(gauge) } }
        },
        instruments.uv?.let { gauge -> { m: Modifier -> InstrumentTile(gauge.label, gauge.valueText, gauge.note, m) { ArcFace(gauge) } } },
        instruments.sun?.let { sun -> { m: Modifier -> InstrumentTile(sun.label, sun.valueText, sun.note, m) { SunFace(sun) } } },
        instruments.airQuality?.let { gauge ->
            { m: Modifier -> InstrumentTile(gauge.label, gauge.valueText, gauge.note, m) { ArcFace(gauge) } }
        }
    )
    Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { tile -> tile(Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InstrumentTile(label: UiText, value: UiText, note: UiText?, modifier: Modifier, face: @Composable () -> Unit) {
    val labelText = label.asString()
    val valueText = value.asString()
    val noteText = note?.asString()
    Column(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = listOfNotNull(labelText, valueText, noteText).joinToString(", ")
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        face()
        EngravedLabel(text = labelText, modifier = Modifier.padding(top = 6.dp))
        Text(text = valueText, style = MaterialTheme.typography.titleLarge)
        if (noteText != null) {
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** 270°'lik küçük kadran: ibre şimdiki değer, varsa pirinç ibre önceki okuma (basınçta 3 saat önce). */
@Composable
private fun ArcFace(gauge: GaugeUi) {
    val instruments = LocalInstrumentColors.current
    val textMeasurer = rememberTextMeasurer()
    val endStyle = MaterialTheme.typography.labelSmall.copy(color = instruments.engraving)
    Canvas(modifier = Modifier.size(FACE)) {
        val radius = face(instruments)
        // Ölçeğin iki ucu, kadranın alttaki boşluğuna kazınır.
        listOf(gauge.scaleMin to DialArc.START, gauge.scaleMax to DialArc.START + DialArc.SWEEP).forEach { (value, angle) ->
            val layout = textMeasurer.measure(value.roundToInt().toString(), endStyle)
            val position = pointOnCircle(center, radius * 0.5f, angle)
            drawText(layout, topLeft = position - Offset(layout.size.width / 2f, layout.size.height / 2f))
        }
        drawTicks(
            center = center,
            radius = radius - 5.dp.toPx(),
            intervals = 20,
            majorEvery = 5,
            minorColor = instruments.tick,
            majorColor = instruments.tickMajor,
            minorLength = 4.dp.toPx(),
            majorLength = 8.dp.toPx(),
            strokeWidth = 1.dp.toPx()
        )
        gauge.previous?.let {
            drawSetHand(
                center = center,
                innerRadius = radius * 0.2f,
                outerRadius = radius - 12.dp.toPx(),
                angle = dialAngle(it, gauge.scaleMin, gauge.scaleMax),
                color = instruments.setHand,
                width = 1.2.dp.toPx()
            )
        }
        drawNeedle(
            center = center,
            length = radius - 10.dp.toPx(),
            angle = dialAngle(gauge.value, gauge.scaleMin, gauge.scaleMax),
            color = instruments.needle,
            width = 3.5.dp.toPx(),
            hubColor = instruments.face
        )
    }
}

/** Pusula: kuzey yukarıda; ok rüzgârın estiği yönü gösterir. */
@Composable
private fun CompassFace(gauge: GaugeUi) {
    val instruments = LocalInstrumentColors.current
    val textMeasurer = rememberTextMeasurer()
    val letterStyle = MaterialTheme.typography.labelSmall.copy(color = instruments.engraving)
    val letters = listOf(
        stringResource(R.string.compass_n) to -90f,
        stringResource(R.string.compass_e) to 0f,
        stringResource(R.string.compass_s) to 90f,
        stringResource(R.string.compass_w) to 180f
    )
    Canvas(modifier = Modifier.size(FACE)) {
        val radius = face(instruments)
        drawTicks(
            center = center,
            radius = radius - 5.dp.toPx(),
            intervals = 24,
            majorEvery = 6,
            minorColor = instruments.tick,
            majorColor = instruments.tickMajor,
            minorLength = 3.dp.toPx(),
            majorLength = 7.dp.toPx(),
            strokeWidth = 1.dp.toPx(),
            start = -90f,
            sweep = 360f
        )
        letters.forEach { (letter, angle) ->
            val layout = textMeasurer.measure(letter, letterStyle)
            val position = pointOnCircle(center, radius - 19.dp.toPx(), angle)
            drawText(layout, topLeft = position - Offset(layout.size.width / 2f, layout.size.height / 2f))
        }
        gauge.direction?.let { toward ->
            drawNeedle(
                center = center,
                length = radius - 16.dp.toPx(),
                angle = toward - 90f,
                color = instruments.needle,
                width = 3.5.dp.toPx(),
                hubColor = instruments.face,
                tail = radius * 0.45f
            )
        }
    }
}

/** Gün ışığı yayı: soldaki uç doğuş, sağdaki batış; yaldız nokta güneşin şimdiki yeri. */
@Composable
private fun SunFace(sun: SunUi) {
    val instruments = LocalInstrumentColors.current
    Canvas(modifier = Modifier.size(FACE)) {
        val radius = face(instruments)
        val arcRadius = radius - 12.dp.toPx()
        val horizon = center.y + 8.dp.toPx()
        val arcCenter = Offset(center.x, horizon)
        drawLine(
            instruments.tickMajor,
            Offset(center.x - radius + 6.dp.toPx(), horizon),
            Offset(center.x + radius - 6.dp.toPx(), horizon),
            1.dp.toPx()
        )
        drawArc(
            color = instruments.tick,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                cap = StrokeCap.Round
            )
        )
        sun.progress?.let { progress ->
            drawArc(
                color = instruments.gilt,
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(instruments.gilt, radius = 5.dp.toPx(), center = pointOnCircle(arcCenter, arcRadius, 180f + 180f * progress))
        }
    }
}

/** Alet yüzü ve çerçevesi; çizim yarıçapını döndürür. */
private fun DrawScope.face(instruments: InstrumentColors): Float {
    val radius = size.minDimension / 2f - 1.dp.toPx()
    drawCircle(instruments.face, radius = radius, center = center)
    drawCircle(instruments.bezel, radius = radius, center = center, style = Stroke(1.dp.toPx()))
    return radius
}

private val FACE = 96.dp
