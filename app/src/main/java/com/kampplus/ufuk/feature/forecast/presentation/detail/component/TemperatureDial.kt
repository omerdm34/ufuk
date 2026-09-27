package com.kampplus.ufuk.feature.forecast.presentation.detail.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.component.WeatherGlyph
import com.kampplus.ufuk.core.ui.component.degreesText
import com.kampplus.ufuk.core.ui.component.dial.DialArc
import com.kampplus.ufuk.core.ui.component.dial.dialAngle
import com.kampplus.ufuk.core.ui.component.dial.drawArcBand
import com.kampplus.ufuk.core.ui.component.dial.drawNeedle
import com.kampplus.ufuk.core.ui.component.dial.drawSetHand
import com.kampplus.ufuk.core.ui.component.dial.drawTicks
import com.kampplus.ufuk.core.ui.component.dial.pointOnCircle
import com.kampplus.ufuk.core.ui.component.glyphColors
import com.kampplus.ufuk.core.ui.motion.rememberReducedMotion
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors
import com.kampplus.ufuk.feature.forecast.presentation.model.TemperatureDialUi
import kotlin.math.roundToInt

/**
 * Ana termometre kadranı. Yüz (ölçek, rakamlar, günün aralığı, dijital okuma) altta, ibreler üstte
 * çizilir; gerçek bir aletteki gibi ibre yazının üzerinden geçer. İbre açılışta yaylı bir salınımla
 * yerine oturur; sistemde animasyonlar kapalıysa doğrudan yerindedir. Bayat veride ibre soluklaşır.
 */
@Composable
fun TemperatureDial(
    dial: TemperatureDialUi,
    condition: String,
    glyph: GlyphKind,
    isDay: Boolean,
    isStale: Boolean,
    modifier: Modifier = Modifier
) {
    val instruments = LocalInstrumentColors.current
    val reducedMotion = rememberReducedMotion()
    val textMeasurer = rememberTextMeasurer()
    val numberStyle = MaterialTheme.typography.labelLarge.copy(color = instruments.engraving)
    val description = dial.description.asString()

    val target = dialAngle(dial.now, dial.scaleMin, dial.scaleMax)
    val needleAngle = remember { Animatable(DialArc.START) }
    LaunchedEffect(target, reducedMotion) {
        if (reducedMotion) {
            needleAngle.snapTo(target)
        } else {
            needleAngle.animateTo(target, spring(dampingRatio = NEEDLE_DAMPING, stiffness = NEEDLE_STIFFNESS))
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side: Dp = minOf(maxWidth, MAX_DIAL)
        Box(
            modifier = Modifier
                .size(side)
                .clearAndSetSemantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 2.dp.toPx()
                drawCircle(color = instruments.face, radius = radius, center = center)
                drawCircle(color = instruments.bezel, radius = radius, center = center, style = Stroke(1.5.dp.toPx()))

                val span = dial.scaleMax - dial.scaleMin
                val unitsPerTick = if (dial.majorStep >= WIDE_STEP) 2 else 1
                drawTicks(
                    center = center,
                    radius = radius - 10.dp.toPx(),
                    intervals = (span / unitsPerTick).roundToInt(),
                    majorEvery = dial.majorStep / unitsPerTick,
                    minorColor = instruments.tick,
                    majorColor = instruments.tickMajor,
                    minorLength = 6.dp.toPx(),
                    majorLength = 13.dp.toPx(),
                    strokeWidth = 1.dp.toPx()
                )

                var value = dial.scaleMin
                while (value <= dial.scaleMax + 0.01f) {
                    val layout = textMeasurer.measure(value.roundToInt().toString(), numberStyle)
                    val position = pointOnCircle(center, radius - 42.dp.toPx(), dialAngle(value, dial.scaleMin, dial.scaleMax))
                    drawText(layout, topLeft = position - Offset(layout.size.width / 2f, layout.size.height / 2f))
                    value += dial.majorStep
                }

                if (dial.todayMin != null && dial.todayMax != null) {
                    drawArcBand(
                        center = center,
                        radius = radius - 28.dp.toPx(),
                        fromAngle = dialAngle(dial.todayMin, dial.scaleMin, dial.scaleMax),
                        toAngle = dialAngle(dial.todayMax, dial.scaleMin, dial.scaleMax),
                        color = instruments.gilt,
                        width = 4.dp.toPx()
                    )
                }
            }

            FaceReadout(
                nowText = dial.nowText,
                condition = condition,
                glyph = glyph,
                isDay = isDay,
                isStale = isStale,
                compact = side < COMPACT_DIAL,
                faceSide = side
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f - 2.dp.toPx()
                dial.yesterday?.let {
                    drawSetHand(
                        center = center,
                        innerRadius = radius * 0.16f,
                        outerRadius = radius - 24.dp.toPx(),
                        angle = dialAngle(it, dial.scaleMin, dial.scaleMax),
                        color = instruments.setHand,
                        width = 1.5.dp.toPx()
                    )
                }
                drawNeedle(
                    center = center,
                    length = radius - 16.dp.toPx(),
                    angle = needleAngle.value,
                    color = instruments.needle.copy(alpha = if (isStale) STALE_ALPHA else 1f),
                    width = 5.dp.toPx(),
                    hubColor = instruments.face
                )
            }
        }
    }
}

@Composable
private fun FaceReadout(
    nowText: String,
    condition: String,
    glyph: GlyphKind,
    isDay: Boolean,
    isStale: Boolean,
    compact: Boolean,
    faceSide: Dp
) {
    // Okuma, kadranın ibre süpürmeyen alt boşluğuna yazılır; ibre hangi değerde olursa olsun üstünden geçmez.
    val color = if (isStale) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = faceSide * 0.21f)
    ) {
        Text(
            text = degreesText(nowText),
            style = if (compact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
            color = color
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WeatherGlyph(kind = glyph, isDay = isDay, size = 22.dp, colors = glyphColors(background = LocalInstrumentColors.current.face))
            EngravedLabel(text = condition, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Kadranın altındaki açıklama: hangi ibre neyi gösteriyor. */
@Composable
fun DialLegend(yesterdayLegend: String?, modifier: Modifier = Modifier) {
    val instruments = LocalInstrumentColors.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        LegendItem(text = stringResource(R.string.detail_now_legend)) {
            drawLine(instruments.needle, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 3.dp.toPx())
        }
        if (yesterdayLegend != null) {
            LegendItem(text = yesterdayLegend) {
                drawSetHand(
                    center = Offset(0f, size.height / 2),
                    innerRadius = 0f,
                    outerRadius = size.width - 3.dp.toPx(),
                    angle = 0f,
                    color = instruments.setHand,
                    width = 1.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun LegendItem(text: String, swatch: DrawScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.size(width = 18.dp, height = 10.dp), onDraw = swatch)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val MAX_DIAL = 340.dp
private val COMPACT_DIAL = 300.dp
private const val WIDE_STEP = 10
private const val STALE_ALPHA = 0.4f
private const val NEEDLE_DAMPING = 0.42f
private const val NEEDLE_STIFFNESS = 55f
