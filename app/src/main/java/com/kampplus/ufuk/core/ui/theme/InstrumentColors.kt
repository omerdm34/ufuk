package com.kampplus.ufuk.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Kadran çizimlerinin renk rolleri. Material rolleri ekranın iskeletini, bunlar alet yüzünü boyar.
 * [needle] ekrandaki tek sıcak vurgudur ve yalnızca canlı (şu anki) okumaya verilir.
 */
@Immutable
data class InstrumentColors(
    val face: Color,
    val bezel: Color,
    val tick: Color,
    val tickMajor: Color,
    val engraving: Color,
    val gilt: Color,
    val needle: Color,
    val setHand: Color,
    val rain: Color,
    val track: Color
)

internal val DarkInstrumentColors = InstrumentColors(
    face = Lacquer2,
    bezel = Lacquer4,
    tick = EngravingDark,
    tickMajor = BoneMuted,
    engraving = BoneMuted,
    gilt = GiltOnDark,
    needle = VermilionOnDark,
    setHand = GiltOnDark,
    rain = SteelOnDark,
    track = Lacquer4
)

internal val LightInstrumentColors = InstrumentColors(
    face = Silver1,
    bezel = Silver4,
    tick = EngravingLight,
    tickMajor = InkMuted,
    engraving = InkMuted,
    gilt = GiltOnLight,
    needle = VermilionOnLight,
    setHand = GiltOnLight,
    rain = SteelOnLight,
    track = Silver4
)

val LocalInstrumentColors = staticCompositionLocalOf { DarkInstrumentColors }
