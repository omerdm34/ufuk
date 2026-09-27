package com.kampplus.ufuk.core.ui.component.dial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * Kadran çiziminin ortak ilkel parçaları. Açılar derece cinsindendir; 0° saat 3 yönü,
 * artış saat yönünde. Klasik alet kadranı 135°'den başlayıp 270° süpürür (altta boşluk kalır).
 */
object DialArc {
    const val START = 135f
    const val SWEEP = 270f
}

/** [value]'yu [min]..[max] ölçeğinde kadran açısına çevirir; ölçek dışı değerler uca yapışır. */
fun dialAngle(value: Float, min: Float, max: Float, start: Float = DialArc.START, sweep: Float = DialArc.SWEEP): Float {
    if (max <= min) return start
    return start + sweep * ((value - min) / (max - min)).coerceIn(0f, 1f)
}

fun pointOnCircle(center: Offset, radius: Float, angleDegrees: Float): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(center.x + radius * cos(radians).toFloat(), center.y + radius * sin(radians).toFloat())
}

/** Eşit aralıklı çentikler; her [majorEvery]'inci çentik uzun ve koyu çizilir. */
fun DrawScope.drawTicks(
    center: Offset,
    radius: Float,
    intervals: Int,
    majorEvery: Int,
    minorColor: Color,
    majorColor: Color,
    minorLength: Float,
    majorLength: Float,
    strokeWidth: Float,
    start: Float = DialArc.START,
    sweep: Float = DialArc.SWEEP
) {
    if (intervals <= 0) return
    for (index in 0..intervals) {
        val angle = start + sweep * index / intervals
        val isMajor = majorEvery > 0 && index % majorEvery == 0
        val length = if (isMajor) majorLength else minorLength
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = pointOnCircle(center, radius - length, angle),
            end = pointOnCircle(center, radius, angle),
            strokeWidth = if (isMajor) strokeWidth * 1.4f else strokeWidth,
            cap = StrokeCap.Butt
        )
    }
}

/** Ölçek üzerinde bir aralığı (ör. bugünün en düşük–en yüksek sıcaklığı) işaretleyen yay. */
fun DrawScope.drawArcBand(center: Offset, radius: Float, fromAngle: Float, toAngle: Float, color: Color, width: Float) {
    drawArc(
        color = color,
        startAngle = fromAngle,
        sweepAngle = (toAngle - fromAngle).coerceAtLeast(0.5f),
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = width, cap = StrokeCap.Round)
    )
}

/** Canlı okuma ibresi: kısa kuyruklu, göbekli. */
fun DrawScope.drawNeedle(
    center: Offset,
    length: Float,
    angle: Float,
    color: Color,
    width: Float,
    hubColor: Color,
    tail: Float = length * 0.18f
) {
    val tip = pointOnCircle(center, length, angle)
    val back = pointOnCircle(center, tail, angle + 180f)
    val side = width / 2f
    val left = pointOnCircle(center, side, angle - 90f)
    val right = pointOnCircle(center, side, angle + 90f)
    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
        lineTo(back.x, back.y)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(path, color)
    drawCircle(color = color, radius = width * 1.3f, center = center)
    drawCircle(color = hubColor, radius = width * 0.55f, center = center)
}

/**
 * Pirinç ayar ibresi: barometrelerde elle önceki okumaya getirilen ince el.
 * Ölçeğin iç kenarında küçük bir baklava uçla biter; canlı ibreyle karışmaz.
 */
fun DrawScope.drawSetHand(center: Offset, innerRadius: Float, outerRadius: Float, angle: Float, color: Color, width: Float) {
    val start = pointOnCircle(center, innerRadius, angle)
    val end = pointOnCircle(center, outerRadius, angle)
    drawLine(color = color, start = start, end = end, strokeWidth = width, cap = StrokeCap.Round)
    val diamond = width * 2.6f
    val mid = pointOnCircle(center, outerRadius - diamond, angle)
    val tip = pointOnCircle(center, outerRadius + diamond * 0.4f, angle)
    val left = pointOnCircle(mid, diamond * 0.7f, angle - 90f)
    val right = pointOnCircle(mid, diamond * 0.7f, angle + 90f)
    val back = pointOnCircle(center, outerRadius - diamond * 2.2f, angle)
    drawPath(
        Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(left.x, left.y)
            lineTo(back.x, back.y)
            lineTo(right.x, right.y)
            close()
        },
        color
    )
}
