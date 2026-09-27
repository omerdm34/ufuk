package com.kampplus.ufuk.feature.forecast.domain.model

import java.time.LocalTime

/**
 * Tahminden çıkarılan, tek cümleye dönüşecek bir gözlem. Sıcaklıklar Celsius'tur;
 * cümleye ve birime çevirme presentation katmanının işidir.
 */
sealed interface WeatherInsight {
    /** Şu an yağış yok, [at] civarında başlaması bekleniyor. */
    data class PrecipitationStarting(
        val at: LocalTime,
        val probability: Int,
        val isSnow: Boolean
    ) : WeatherInsight

    /** Şu an yağış var, [at] civarında dinmesi bekleniyor. */
    data class PrecipitationEasing(
        val at: LocalTime
    ) : WeatherInsight

    data class StrongWind(
        val gustKmh: Double
    ) : WeatherInsight

    /** Önümüzdeki saatlerde sıcaklık sıfırın altına iniyor. */
    data class FrostAhead(
        val minimumC: Double,
        val at: LocalTime
    ) : WeatherInsight

    data class HighUv(
        val from: LocalTime,
        val until: LocalTime,
        val peak: Double
    ) : WeatherInsight

    /** Hissedilen sıcaklık ölçülenden belirgin farklı (rüzgâr ya da nem etkisi). */
    data class FeelsDifferent(
        val apparentC: Double,
        val deltaC: Double
    ) : WeatherInsight

    /** Dün aynı saate göre fark (pozitif = daha sıcak). */
    data class ComparedToYesterday(
        val deltaC: Double
    ) : WeatherInsight

    /** Yarının en yüksek sıcaklığı bugüne göre (pozitif = daha sıcak). */
    data class TomorrowChange(
        val deltaC: Double
    ) : WeatherInsight

    /** Hiçbir kural tetiklenmediğinde: kullanıcıya "değişiklik yok" demek de bir bilgidir. */
    data object Steady : WeatherInsight
}
