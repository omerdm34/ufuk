package com.kampplus.ufuk.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.dial.drawNeedle
import com.kampplus.ufuk.core.ui.component.dial.drawTicks
import com.kampplus.ufuk.core.ui.motion.rememberReducedMotion
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors

/** Yükleniyor: ölçeğini tarayan bir ibre ("alet ısınıyor"). Hareket kapalıysa ibre sabit durur. */
@Composable
fun LoadingView(modifier: Modifier = Modifier, label: String = stringResource(R.string.state_loading)) {
    val instruments = LocalInstrumentColors.current
    val reducedMotion = rememberReducedMotion()
    val sweep by rememberInfiniteTransition(label = "loading").animateFloat(
        initialValue = 135f,
        targetValue = 405f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "needle"
    )
    Column(
        modifier = modifier
            .padding(24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val radius = size.minDimension / 2f
            drawTicks(
                center = center,
                radius = radius,
                intervals = 18,
                majorEvery = 6,
                minorColor = instruments.tick,
                majorColor = instruments.tickMajor,
                minorLength = radius * 0.12f,
                majorLength = radius * 0.22f,
                strokeWidth = 1.dp.toPx()
            )
            drawNeedle(
                center = center,
                length = radius * 0.72f,
                angle = if (reducedMotion) 270f else sweep,
                color = instruments.needle,
                width = 2.5.dp.toPx(),
                hubColor = instruments.face
            )
        }
        EngravedLabel(text = label)
    }
}

@Composable
fun ErrorView(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    MessageView(
        title = stringResource(R.string.state_error_title),
        message = message,
        modifier = modifier,
        glyph = GlyphKind.Unknown,
        actionLabel = onRetry?.let { stringResource(R.string.action_retry) },
        onAction = onRetry
    )
}

@Composable
fun EmptyView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    glyph: GlyphKind = GlyphKind.PartlyCloudy,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    MessageView(title = title, message = message, modifier = modifier, glyph = glyph, actionLabel = actionLabel, onAction = onAction)
}

@Composable
private fun MessageView(
    title: String,
    message: String,
    glyph: GlyphKind,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeatherGlyph(kind = glyph, isDay = true, size = 56.dp, colors = glyphColors(line = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) { Text(actionLabel) }
        }
    }
}
