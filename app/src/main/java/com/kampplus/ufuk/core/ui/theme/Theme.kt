package com.kampplus.ufuk.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = GiltOnDark,
    onPrimary = Lacquer0,
    primaryContainer = Lacquer4,
    onPrimaryContainer = Bone,
    secondary = SteelOnDark,
    onSecondary = Lacquer0,
    secondaryContainer = Lacquer4,
    onSecondaryContainer = Bone,
    tertiary = VermilionOnDark,
    onTertiary = Lacquer0,
    background = Lacquer1,
    onBackground = Bone,
    surface = Lacquer1,
    onSurface = Bone,
    surfaceVariant = Lacquer3,
    onSurfaceVariant = BoneMuted,
    surfaceContainerLowest = Lacquer0,
    surfaceContainerLow = Lacquer1,
    surfaceContainer = Lacquer2,
    surfaceContainerHigh = Lacquer3,
    surfaceContainerHighest = Lacquer4,
    inverseSurface = Bone,
    inverseOnSurface = Lacquer1,
    inversePrimary = GiltOnLight,
    outline = EngravingDark,
    outlineVariant = Lacquer3,
    error = ErrorOnDark,
    onError = Lacquer0
)

private val LightColors = lightColorScheme(
    primary = GiltOnLight,
    onPrimary = Silver1,
    primaryContainer = Silver3,
    onPrimaryContainer = Ink,
    secondary = SteelOnLight,
    onSecondary = Silver1,
    secondaryContainer = Silver3,
    onSecondaryContainer = Ink,
    tertiary = VermilionOnLight,
    onTertiary = Silver1,
    background = Silver0,
    onBackground = Ink,
    surface = Silver0,
    onSurface = Ink,
    surfaceVariant = Silver3,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Silver1,
    surfaceContainerLow = Silver1,
    surfaceContainer = Silver2,
    surfaceContainerHigh = Silver3,
    surfaceContainerHighest = Silver4,
    inverseSurface = Lacquer2,
    inverseOnSurface = Bone,
    inversePrimary = GiltOnDark,
    outline = EngravingLight,
    outlineVariant = Silver3,
    error = ErrorOnLight,
    onError = Silver1
)

/** Alet gövdesi gibi: küçük köşeler. Yuvarlak olan yalnızca kadranlardır. */
private val UfukShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

/**
 * Tema sistem ayarını izler. Dinamik renk (Material You) bilinçli olarak kullanılmaz:
 * kadran dünyası tek bir sıcak vurguya (canlı okuma) dayanır, duvar kâğıdı rengi bunu bozar.
 */
@Composable
fun UfukTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalInstrumentColors provides if (darkTheme) DarkInstrumentColors else LightInstrumentColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = UfukTypography,
            shapes = UfukShapes,
            content = content
        )
    }
}
