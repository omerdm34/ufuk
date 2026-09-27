package com.kampplus.ufuk.feature.forecast.domain.policy

/** WHO UV indeksi kademeleri. */
enum class UvBand {
    Low,
    Moderate,
    High,
    VeryHigh,
    Extreme;

    companion object {
        fun of(index: Double): UvBand = when {
            index < 3 -> Low
            index < 6 -> Moderate
            index < 8 -> High
            index < 11 -> VeryHigh
            else -> Extreme
        }
    }
}

/** Avrupa Hava Kalitesi İndeksi (EEA) kademeleri. */
enum class AirQualityBand {
    Good,
    Fair,
    Moderate,
    Poor,
    VeryPoor,
    ExtremelyPoor;

    companion object {
        fun of(europeanAqi: Int): AirQualityBand = when {
            europeanAqi < 20 -> Good
            europeanAqi < 40 -> Fair
            europeanAqi < 60 -> Moderate
            europeanAqi < 80 -> Poor
            europeanAqi < 100 -> VeryPoor
            else -> ExtremelyPoor
        }
    }
}

/** Barometre okuması: 3 saatte 1 hPa'dan büyük değişim eğilim sayılır. */
enum class PressureTrend {
    Rising,
    Steady,
    Falling;

    companion object {
        private const val THRESHOLD_HPA = 1.0

        fun of(nowHpa: Double, earlierHpa: Double): PressureTrend {
            val delta = nowHpa - earlierHpa
            return when {
                delta >= THRESHOLD_HPA -> Rising
                delta <= -THRESHOLD_HPA -> Falling
                else -> Steady
            }
        }
    }
}

/** Rüzgârın geldiği yön, 8 ana ve ara yön. 0° = kuzeyden. */
enum class CompassPoint {
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NorthWest;

    companion object {
        private const val SECTOR = 45.0

        fun of(degrees: Int): CompassPoint = entries[(((degrees % 360 + 360) % 360 + SECTOR / 2) / SECTOR).toInt() % entries.size]
    }
}
