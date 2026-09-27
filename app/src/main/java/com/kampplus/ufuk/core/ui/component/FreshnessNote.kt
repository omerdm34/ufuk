package com.kampplus.ufuk.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors

/**
 * Verinin tazeliği. Canlı veride dolu yaldız nokta, bayat veride içi boş halka: renk tek başına
 * anlam taşımaz, metin her zaman yaşı söyler.
 */
@Composable
fun FreshnessNote(text: String, isStale: Boolean, modifier: Modifier = Modifier) {
    val instruments = LocalInstrumentColors.current
    val color = if (isStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.size(8.dp)) {
            if (isStale) {
                drawCircle(color = color, radius = size.minDimension / 2f - 1.dp.toPx(), style = Stroke(1.5.dp.toPx()))
            } else {
                drawCircle(color = instruments.gilt)
            }
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
