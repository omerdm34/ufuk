package com.kampplus.ufuk.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.core.ui.theme.LocalInstrumentColors

/** Hava durumunun çizilecek biçimi. Emoji yerine tek çizgi kalınlığında, temaya uyan ikonlar. */
enum class GlyphKind {
    Clear,
    PartlyCloudy,
    Cloudy,
    Fog,
    Drizzle,
    Rain,
    Snow,
    Thunderstorm,
    Unknown
}

data class GlyphColors(
    val line: Color,
    val sun: Color,
    val rain: Color,
    val background: Color
)

@Composable
fun glyphColors(line: Color = MaterialTheme.colorScheme.onSurface, background: Color = MaterialTheme.colorScheme.surface): GlyphColors {
    val instruments = LocalInstrumentColors.current
    return GlyphColors(line = line, sun = instruments.gilt, rain = instruments.rain, background = background)
}

/** Erişilebilirlik metni çağıran tarafta verilir (durum adı zaten yanında yazılıdır). */
@Composable
fun WeatherGlyph(kind: GlyphKind, isDay: Boolean, modifier: Modifier = Modifier, size: Dp = 28.dp, colors: GlyphColors = glyphColors()) {
    Canvas(modifier = modifier.size(size)) {
        drawWeatherGlyph(kind = kind, isDay = isDay, colors = colors, topLeft = Offset.Zero, side = this.size.minDimension)
    }
}

/** 24 birimlik bir ızgarada tasarlanmış ikonu [side] boyutunda [topLeft]'e çizer. Grafiklerde de kullanılır. */
fun DrawScope.drawWeatherGlyph(kind: GlyphKind, isDay: Boolean, colors: GlyphColors, topLeft: Offset, side: Float) {
    translate(topLeft.x, topLeft.y) {
        scale(side / GRID, side / GRID, pivot = Offset.Zero) {
            val stroke = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            when (kind) {
                GlyphKind.Clear -> if (isDay) {
                    sun(
                        Offset(12f, 12f),
                        4.6f,
                        colors.sun,
                        stroke
                    )
                } else {
                    moon(Offset(12f, 12f), 6.5f, colors.sun, stroke)
                }
                GlyphKind.PartlyCloudy -> {
                    if (isDay) {
                        sun(
                            Offset(9f, 8.5f),
                            3.2f,
                            colors.sun,
                            stroke,
                            rays = 2.2f
                        )
                    } else {
                        moon(Offset(9f, 8f), 4.6f, colors.sun, stroke)
                    }
                    cloud(Offset(1.5f, 2f), colors, stroke)
                }
                GlyphKind.Cloudy, GlyphKind.Unknown -> cloud(Offset(-0.5f, 0f), colors, stroke)
                GlyphKind.Fog -> fog(colors.line, stroke)
                GlyphKind.Drizzle -> {
                    cloud(Offset(-0.5f, -3f), colors, stroke)
                    listOf(8f, 12f, 16f).forEachIndexed { i, x -> drawCircle(colors.rain, 0.9f, Offset(x, 18.5f + (i % 2) * 2f)) }
                }
                GlyphKind.Rain -> {
                    cloud(Offset(-0.5f, -3f), colors, stroke)
                    listOf(8.5f, 12.5f, 16.5f).forEach { x ->
                        drawLine(colors.rain, Offset(x, 17f), Offset(x - 1.6f, 21.5f), strokeWidth = 1.6f, cap = StrokeCap.Round)
                    }
                }
                GlyphKind.Snow -> {
                    cloud(Offset(-0.5f, -3f), colors, stroke)
                    listOf(Offset(8f, 18.5f), Offset(12.5f, 20.5f), Offset(17f, 18.5f)).forEach { flake(it, colors.line) }
                }
                GlyphKind.Thunderstorm -> {
                    cloud(Offset(-0.5f, -3f), colors, stroke)
                    val bolt = Path().apply {
                        moveTo(13f, 15f)
                        lineTo(9.8f, 19.4f)
                        lineTo(12.4f, 19.4f)
                        lineTo(10.8f, 23f)
                        lineTo(15.2f, 17.8f)
                        lineTo(12.6f, 17.8f)
                        close()
                    }
                    drawPath(bolt, colors.sun)
                }
            }
        }
    }
}

private const val GRID = 24f

private fun DrawScope.sun(center: Offset, radius: Float, color: Color, stroke: Stroke, rays: Float = 3f) {
    drawCircle(color = color, radius = radius, center = center, style = stroke)
    for (i in 0 until 8) {
        val angle = Math.toRadians(i * 45.0)
        val from = radius + 1.9f
        val to = from + rays
        drawLine(
            color = color,
            start = Offset(center.x + from * Math.cos(angle).toFloat(), center.y + from * Math.sin(angle).toFloat()),
            end = Offset(center.x + to * Math.cos(angle).toFloat(), center.y + to * Math.sin(angle).toFloat()),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.moon(center: Offset, radius: Float, color: Color, stroke: Stroke) {
    val disc = Path().apply { addOval(Rect(center, radius)) }
    val bite = Path().apply { addOval(Rect(Offset(center.x + radius * 0.55f, center.y - radius * 0.45f), radius * 0.95f)) }
    drawPath(Path.combine(PathOperation.Difference, disc, bite), color, style = stroke)
}

/** Üç daire ve bir tabandan birleşik bulut; arkasındaki güneşi örtmek için zemin rengiyle doldurulur. */
private fun DrawScope.cloud(offset: Offset, colors: GlyphColors, stroke: Stroke) {
    fun circle(x: Float, y: Float, r: Float) = Path().apply { addOval(Rect(Offset(x + offset.x, y + offset.y), r)) }
    var shape = Path.combine(PathOperation.Union, circle(8.5f, 14.2f, 4f), circle(13.5f, 11.2f, 5.2f))
    shape = Path.combine(PathOperation.Union, shape, circle(18f, 15f, 3.2f))
    shape = Path.combine(
        PathOperation.Union,
        shape,
        Path().apply { addRect(Rect(8.5f + offset.x, 14.2f + offset.y, 18f + offset.x, 18.2f + offset.y)) }
    )
    drawPath(shape, colors.background)
    drawPath(shape, colors.line, style = stroke)
}

private fun DrawScope.fog(color: Color, stroke: Stroke) {
    listOf(Triple(4f, 20f, 8f), Triple(6f, 18f, 12f), Triple(3f, 17f, 16f), Triple(7f, 21f, 20f)).forEach { (from, to, y) ->
        drawLine(color, Offset(from, y), Offset(to, y), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

private fun DrawScope.flake(center: Offset, color: Color) {
    for (i in 0 until 3) {
        val angle = Math.toRadians(i * 60.0 + 90)
        val dx = 1.5f * Math.cos(angle).toFloat()
        val dy = 1.5f * Math.sin(angle).toFloat()
        drawLine(
            color,
            Offset(center.x - dx, center.y - dy),
            Offset(center.x + dx, center.y + dy),
            strokeWidth = 1.1f,
            cap = StrokeCap.Round
        )
    }
}
