package com.kampplus.ufuk.feature.forecast.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.core.ui.component.WeatherGlyph
import com.kampplus.ufuk.core.ui.component.degreesText
import com.kampplus.ufuk.core.ui.component.glyphColors
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowUi

/**
 * Yer satırı. Kutu yok: hiyerarşiyi yazı boyutu kurar, en büyük öğe sıcaklıktır. Altındaki kısa
 * çubuk, şimdiki sıcaklığın bugünün aralığında nerede olduğunu gösterir (detaydaki çubukların küçüğü).
 */
@Composable
fun PlaceRow(row: PlaceRowUi, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    val description = row.description.asString()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .heightIn(min = 76.dp)
            .padding(start = 16.dp, end = if (trailing == null) 16.dp else 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        WeatherGlyph(
            kind = row.glyph,
            isDay = row.isDay,
            size = 32.dp,
            colors = glyphColors(background = MaterialTheme.colorScheme.background)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (row.isDeviceLocation) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = row.title.asString(),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!row.region.isNullOrEmpty()) {
                Text(
                    text = row.region,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = row.detail.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = degreesText(row.temperatureText ?: "–"), style = MaterialTheme.typography.displaySmall)
            if (row.temperature != null && row.todayMin != null && row.todayMax != null) {
                TodayRangeBar(now = row.temperature, min = row.todayMin, max = row.todayMax)
            }
            row.rangeText?.let {
                Text(
                    text = degreesText(it.asString()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
private fun TodayRangeBar(now: Float, min: Float, max: Float) {
    val instruments = LocalInstrumentColors.current
    Canvas(modifier = Modifier.width(56.dp).padding(vertical = 2.dp).size(width = 56.dp, height = 6.dp)) {
        val y = size.height / 2f
        val span = (max - min).takeIf { it > 0f } ?: 1f
        val x = ((now - min) / span).coerceIn(0f, 1f) * size.width
        drawLine(instruments.gilt, Offset(0f, y), Offset(size.width, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(instruments.face, radius = 4.dp.toPx(), center = Offset(x, y))
        drawCircle(instruments.needle, radius = 3.dp.toPx(), center = Offset(x, y))
    }
}
