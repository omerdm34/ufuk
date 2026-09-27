package com.kampplus.ufuk.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kampplus.ufuk.R

/** Kadran rakamları ve kazıma etiketler: Barlow Condensed (OFL, res/font). Gövde metni sistem yazısıdır. */
val Barlow = FontFamily(
    Font(R.font.barlow_condensed_light, FontWeight.Light),
    Font(R.font.barlow_condensed_regular, FontWeight.Normal),
    Font(R.font.barlow_condensed_medium, FontWeight.Medium),
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold)
)

/** Tablo gibi hizalanan rakamlar: sıcaklıklar alt alta geldiğinde titremez. */
private const val TABULAR = "tnum"

private val base = Typography()

private fun barlow(weight: FontWeight, size: Int, lineHeight: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = Barlow,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = TABULAR
)

internal val UfukTypography = Typography(
    displayLarge = barlow(FontWeight.Light, size = 104, lineHeight = 104, tracking = -0.02),
    displayMedium = barlow(FontWeight.Light, size = 56, lineHeight = 58, tracking = -0.01),
    displaySmall = barlow(FontWeight.Light, size = 40, lineHeight = 44),
    headlineLarge = barlow(FontWeight.Medium, size = 34, lineHeight = 40),
    headlineMedium = barlow(FontWeight.Medium, size = 28, lineHeight = 34),
    headlineSmall = barlow(FontWeight.Normal, size = 24, lineHeight = 30),
    titleLarge = barlow(FontWeight.Medium, size = 24, lineHeight = 30),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge,
    bodyMedium = base.bodyMedium,
    bodySmall = base.bodySmall,
    // Kazıma etiketler: dar, harf aralığı açık; büyük harfe EngravedLabel çevirir.
    labelLarge = barlow(FontWeight.Medium, size = 16, lineHeight = 20, tracking = 0.04),
    labelMedium = barlow(FontWeight.Medium, size = 13, lineHeight = 16, tracking = 0.1),
    labelSmall = barlow(FontWeight.Medium, size = 11, lineHeight = 14, tracking = 0.12)
)
