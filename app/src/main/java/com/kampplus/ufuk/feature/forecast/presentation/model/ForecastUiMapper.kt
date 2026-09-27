package com.kampplus.ufuk.feature.forecast.presentation.model

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.ui.format.UnitFormatter
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.model.Forecast
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherInsight
import com.kampplus.ufuk.feature.forecast.domain.model.hoursAgo
import com.kampplus.ufuk.feature.forecast.domain.model.sameHourYesterday
import com.kampplus.ufuk.feature.forecast.domain.model.today
import com.kampplus.ufuk.feature.forecast.domain.model.tomorrow
import com.kampplus.ufuk.feature.forecast.domain.model.upcomingDays
import com.kampplus.ufuk.feature.forecast.domain.model.upcomingHours
import com.kampplus.ufuk.feature.forecast.domain.policy.AirQualityBand
import com.kampplus.ufuk.feature.forecast.domain.policy.CompassPoint
import com.kampplus.ufuk.feature.forecast.domain.policy.InsightGenerator
import com.kampplus.ufuk.feature.forecast.domain.policy.PressureTrend
import com.kampplus.ufuk.feature.forecast.domain.policy.UvBand
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/** Domain tahminini, kullanıcının birimleriyle, detay ekranının modeline çevirir. */
class ForecastUiMapper @Inject constructor(
    private val conditions: ConditionUiRegistry,
    private val insightGenerator: InsightGenerator
) {
    fun toUiModel(place: Place, forecast: Forecast, units: UnitSettings, locale: Locale = Locale.getDefault()): ForecastUiModel {
        val format = UnitFormatter(units)
        val current = forecast.current
        val condition = conditions.resolve(current.weatherCode)
        val today = forecast.today()
        val insights = insightGenerator.generate(forecast).map { it.toUiText(format) }
        val title = placeTitle(place)
        return ForecastUiModel(
            title = title,
            subtitle = listOfNotNull(place.region, place.country).distinct().joinToString(", "),
            dial = dial(forecast, format, condition),
            condition = condition.label,
            glyph = condition.glyph,
            isDay = current.isDay,
            highLow = today?.let {
                UiText.Resource(R.string.detail_high_low, format.temperature(it.maxTemperatureC), format.temperature(it.minTemperatureC))
            },
            insights = insights,
            readings = readings(forecast, format),
            hourly = hourly(forecast, format),
            daily = daily(forecast, format, locale),
            instruments = instruments(forecast, format),
            shareText = UiText.Resource(
                R.string.share_text,
                title,
                format.temperature(current.temperatureC),
                condition.label,
                today?.let { format.temperature(it.minTemperatureC) } ?: "–",
                today?.let { format.temperature(it.maxTemperatureC) } ?: "–",
                insights.first()
            )
        )
    }

    fun placeTitle(place: Place): UiText =
        if (place.isDeviceLocation && place.name.isBlank()) UiText.Resource(R.string.my_location) else UiText.Dynamic(place.name)

    private fun dial(forecast: Forecast, format: UnitFormatter, condition: ConditionUi): TemperatureDialUi {
        val unit = format.units.temperature
        val today = forecast.today()
        val yesterday = forecast.sameHourYesterday()
        val now = unit.fromCelsius(forecast.current.temperatureC)
        val todayMin = today?.let { unit.fromCelsius(it.minTemperatureC) }
        val todayMax = today?.let { unit.fromCelsius(it.maxTemperatureC) }
        val yesterdayValue = yesterday?.let { unit.fromCelsius(it.temperatureC) }
        val readings = listOfNotNull(now, todayMin, todayMax, yesterdayValue)
        val step = if ((readings.max() - readings.min()) > WIDE_SPAN) WIDE_STEP else NARROW_STEP
        var low = floor((readings.min() - SCALE_PADDING) / step) * step
        var high = ceil((readings.max() + SCALE_PADDING) / step) * step
        while (high - low < MIN_SPAN_STEPS * step) {
            low -= step
            if (high - low < MIN_SPAN_STEPS * step) high += step
        }
        val nowText = format.temperature(forecast.current.temperatureC)
        val minText = today?.let { format.temperature(it.minTemperatureC) }
        val maxText = today?.let { format.temperature(it.maxTemperatureC) }
        val yesterdayText = yesterday?.let { format.temperature(it.temperatureC) }
        return TemperatureDialUi(
            now = now.toFloat(),
            nowText = nowText,
            scaleMin = low.toFloat(),
            scaleMax = high.toFloat(),
            majorStep = step.toInt(),
            todayMin = todayMin?.toFloat(),
            todayMax = todayMax?.toFloat(),
            yesterday = yesterdayValue?.toFloat(),
            yesterdayLegend = yesterdayText?.let { UiText.Resource(R.string.detail_yesterday_legend, it) },
            description = when {
                minText == null || maxText == null -> UiText.Resource(R.string.a11y_dial_now, nowText, condition.label)
                yesterdayText == null -> UiText.Resource(R.string.a11y_dial, nowText, condition.label, minText, maxText)
                else -> UiText.Resource(R.string.a11y_dial_yesterday, nowText, condition.label, minText, maxText, yesterdayText)
            }
        )
    }

    private fun readings(forecast: Forecast, format: UnitFormatter): List<ReadingUi> {
        val current = forecast.current
        val rainChance = forecast.upcomingHours(RAIN_WINDOW_HOURS).mapNotNull { it.precipitationProbability }.maxOrNull()
        return listOfNotNull(
            current.apparentTemperatureC?.let {
                ReadingUi(UiText.Resource(R.string.reading_feels), UiText.Dynamic(format.temperature(it)))
            },
            rainChance?.let { ReadingUi(UiText.Resource(R.string.reading_rain), percent(it)) },
            current.windSpeedKmh?.let { ReadingUi(UiText.Resource(R.string.reading_wind), format.windSpeed(it)) }
        )
    }

    private fun hourly(forecast: Forecast, format: UnitFormatter): List<HourlyUi> =
        forecast.upcomingHours(HOURLY_COUNT).mapIndexed { index, hour ->
            val condition = conditions.resolve(hour.weatherCode)
            HourlyUi(
                label = if (index == 0) UiText.Resource(R.string.hour_now) else UiText.Dynamic(hour.time.format(HOUR_FORMAT)),
                isNow = index == 0,
                temperature = format.units.temperature.fromCelsius(
                    if (index ==
                        0
                    ) {
                        forecast.current.temperatureC
                    } else {
                        hour.temperatureC
                    }
                ).toFloat(),
                temperatureText = format.temperature(if (index == 0) forecast.current.temperatureC else hour.temperatureC),
                glyph = condition.glyph,
                isDay = hour.isDay,
                precipitationProbability = hour.precipitationProbability
            )
        }

    private fun daily(forecast: Forecast, format: UnitFormatter, locale: Locale): DailyRangeUi {
        val unit = format.units.temperature
        val dayFormat = DateTimeFormatter.ofPattern("EEE d", locale)
        val days = forecast.upcomingDays().take(DAILY_COUNT).mapIndexed { index, day ->
            val condition = conditions.resolve(day.weatherCode)
            val label = when (index) {
                0 -> UiText.Resource(R.string.day_today)
                1 -> UiText.Resource(R.string.day_tomorrow)
                else -> UiText.Dynamic(day.date.format(dayFormat).replaceFirstChar { it.titlecase(locale) })
            }
            val precipitation = day.precipitationProbability?.takeIf { it >= VISIBLE_PRECIPITATION }?.let(::percent)
            val minText = format.temperature(day.minTemperatureC)
            val maxText = format.temperature(day.maxTemperatureC)
            DayUi(
                label = label,
                isToday = index == 0,
                glyph = condition.glyph,
                min = unit.fromCelsius(day.minTemperatureC).toFloat(),
                max = unit.fromCelsius(day.maxTemperatureC).toFloat(),
                minText = minText,
                maxText = maxText,
                precipitation = precipitation,
                description = if (precipitation == null) {
                    UiText.Resource(R.string.a11y_day, label, condition.label, minText, maxText)
                } else {
                    UiText.Resource(R.string.a11y_day_precipitation, label, condition.label, minText, maxText, precipitation)
                }
            )
        }
        return DailyRangeUi(
            days = days,
            scaleMin = days.minOfOrNull { it.min } ?: 0f,
            scaleMax = days.maxOfOrNull { it.max } ?: 0f,
            now = unit.fromCelsius(forecast.current.temperatureC).toFloat()
        )
    }

    private fun instruments(forecast: Forecast, format: UnitFormatter): InstrumentsUi {
        val current = forecast.current
        val today = forecast.today()
        return InstrumentsUi(
            wind = current.windSpeedKmh?.let { speed ->
                val from = current.windDirectionDegrees?.let { compassFrom(CompassPoint.of(it)) }
                val gust = current.windGustsKmh?.let(format::windSpeed)
                GaugeUi(
                    label = UiText.Resource(R.string.instrument_wind),
                    valueText = format.windSpeed(speed),
                    note = when {
                        from != null && gust != null -> UiText.Resource(R.string.wind_note, from, gust)
                        from != null -> from
                        gust != null -> UiText.Resource(R.string.wind_note_gust, gust)
                        else -> null
                    },
                    value = format.windValue(speed).toFloat(),
                    scaleMin = 0f,
                    scaleMax = format.windValue(WIND_SCALE_KMH).toFloat(),
                    // Ok, rüzgârın geldiği yönün tersini, yani estiği yönü gösterir.
                    direction = current.windDirectionDegrees?.let { ((it + HALF_TURN) % FULL_TURN).toFloat() }
                )
            },
            humidity = current.humidityPercent?.let { humidity ->
                GaugeUi(
                    label = UiText.Resource(R.string.instrument_humidity),
                    valueText = percent(humidity),
                    note = current.dewPointC?.let { UiText.Resource(R.string.humidity_note, format.temperature(it)) },
                    value = humidity.toFloat(),
                    scaleMin = 0f,
                    scaleMax = 100f
                )
            },
            pressure = current.pressureHpa?.let { pressure ->
                val earlier = forecast.hoursAgo(PRESSURE_TREND_HOURS)?.pressureHpa
                GaugeUi(
                    label = UiText.Resource(R.string.instrument_pressure),
                    valueText = UiText.Resource(R.string.pressure_value, pressure.roundToInt()),
                    note = earlier?.let { UiText.Resource(PressureTrend.of(pressure, it).labelRes()) },
                    value = pressure.toFloat(),
                    scaleMin = PRESSURE_MIN,
                    scaleMax = PRESSURE_MAX,
                    previous = earlier?.toFloat()
                )
            },
            uv = current.uvIndex?.let { uv ->
                val band = UiText.Resource(UvBand.of(uv).labelRes())
                GaugeUi(
                    label = UiText.Resource(R.string.instrument_uv),
                    valueText = UiText.Dynamic(uv.roundToInt().toString()),
                    note = today?.uvIndexMax?.let { UiText.Resource(R.string.uv_note, band, it.roundToInt()) } ?: band,
                    value = uv.toFloat(),
                    scaleMin = 0f,
                    scaleMax = UV_MAX
                )
            },
            sun = sun(forecast),
            airQuality = forecast.airQuality?.let { air ->
                val band = UiText.Resource(AirQualityBand.of(air.europeanAqi).labelRes())
                GaugeUi(
                    label = UiText.Resource(R.string.instrument_air),
                    valueText = UiText.Dynamic(air.europeanAqi.toString()),
                    note = air.pm25?.let { UiText.Resource(R.string.air_note, band, it.roundToInt()) } ?: band,
                    value = air.europeanAqi.toFloat(),
                    scaleMin = 0f,
                    scaleMax = AQI_MAX
                )
            }
        )
    }

    private fun sun(forecast: Forecast): SunUi? {
        val today = forecast.today() ?: return null
        val sunrise = today.sunrise ?: return null
        val sunset = today.sunset ?: return null
        val now = forecast.current.time
        val daylight = Duration.between(sunrise, sunset)
        val note = UiText.Resource(R.string.sun_daylight, daylight.toHours().toInt(), (daylight.toMinutes() % MINUTES_PER_HOUR).toInt())
        val label = UiText.Resource(R.string.instrument_sun)
        return when {
            now.isBefore(sunrise) -> SunUi(label, UiText.Resource(R.string.sun_rises, sunrise.toLocalTime().hhmm()), note, progress = null)
            now.isBefore(sunset) -> SunUi(
                label = label,
                valueText = UiText.Resource(R.string.sun_sets, sunset.toLocalTime().hhmm()),
                note = note,
                progress = Duration.between(sunrise, now).toMinutes().toFloat() / daylight.toMinutes().coerceAtLeast(1)
            )
            else -> SunUi(
                label = label,
                valueText = forecast.tomorrow()?.sunrise?.let { UiText.Resource(R.string.sun_rises, it.toLocalTime().hhmm()) }
                    ?: UiText.Resource(R.string.sun_set_at, sunset.toLocalTime().hhmm()),
                note = note,
                progress = null
            )
        }
    }

    private fun WeatherInsight.toUiText(format: UnitFormatter): UiText = when (this) {
        is WeatherInsight.PrecipitationStarting -> UiText.Resource(
            if (isSnow) R.string.insight_snow_starting else R.string.insight_rain_starting,
            at.hhmm(),
            percent(probability)
        )
        is WeatherInsight.PrecipitationEasing -> UiText.Resource(R.string.insight_precipitation_easing, at.hhmm())
        is WeatherInsight.StrongWind -> UiText.Resource(R.string.insight_strong_wind, format.windSpeed(gustKmh))
        is WeatherInsight.FrostAhead -> UiText.Resource(R.string.insight_frost, at.hhmm(), format.temperature(minimumC))
        is WeatherInsight.HighUv -> UiText.Resource(R.string.insight_high_uv, from.hhmm(), until.hhmm(), peak.roundToInt())
        is WeatherInsight.FeelsDifferent -> UiText.Resource(
            if (deltaC < 0) R.string.insight_feels_colder else R.string.insight_feels_warmer,
            format.temperature(apparentC)
        )
        is WeatherInsight.ComparedToYesterday -> UiText.Resource(
            if (deltaC > 0) R.string.insight_warmer_than_yesterday else R.string.insight_colder_than_yesterday,
            format.temperatureDelta(deltaC)
        )
        is WeatherInsight.TomorrowChange -> UiText.Resource(
            if (deltaC > 0) R.string.insight_tomorrow_warmer else R.string.insight_tomorrow_colder,
            format.temperatureDelta(deltaC)
        )
        WeatherInsight.Steady -> UiText.Resource(R.string.insight_steady)
    }

    private fun percent(value: Int): UiText = UiText.Resource(R.string.percent_value, value)

    private fun compassFrom(point: CompassPoint): UiText = UiText.Resource(
        when (point) {
            CompassPoint.North -> R.string.wind_from_north
            CompassPoint.NorthEast -> R.string.wind_from_north_east
            CompassPoint.East -> R.string.wind_from_east
            CompassPoint.SouthEast -> R.string.wind_from_south_east
            CompassPoint.South -> R.string.wind_from_south
            CompassPoint.SouthWest -> R.string.wind_from_south_west
            CompassPoint.West -> R.string.wind_from_west
            CompassPoint.NorthWest -> R.string.wind_from_north_west
        }
    )

    private fun PressureTrend.labelRes() = when (this) {
        PressureTrend.Rising -> R.string.pressure_rising
        PressureTrend.Steady -> R.string.pressure_steady
        PressureTrend.Falling -> R.string.pressure_falling
    }

    private fun UvBand.labelRes() = when (this) {
        UvBand.Low -> R.string.uv_low
        UvBand.Moderate -> R.string.uv_moderate
        UvBand.High -> R.string.uv_high
        UvBand.VeryHigh -> R.string.uv_very_high
        UvBand.Extreme -> R.string.uv_extreme
    }

    private fun AirQualityBand.labelRes() = when (this) {
        AirQualityBand.Good -> R.string.aqi_good
        AirQualityBand.Fair -> R.string.aqi_fair
        AirQualityBand.Moderate -> R.string.aqi_moderate
        AirQualityBand.Poor -> R.string.aqi_poor
        AirQualityBand.VeryPoor -> R.string.aqi_very_poor
        AirQualityBand.ExtremelyPoor -> R.string.aqi_extremely_poor
    }

    private fun LocalTime.hhmm(): String = format(HOUR_FORMAT)

    private companion object {
        const val HOURLY_COUNT = 24
        const val MINUTES_PER_HOUR = 60
        const val DAILY_COUNT = 10

        /** Özet kuralıyla aynı pencere: içinde bulunulan saat + sonraki 12 saat. */
        const val RAIN_WINDOW_HOURS = InsightGenerator.LOOKAHEAD_HOURS + 1
        const val VISIBLE_PRECIPITATION = 20
        const val PRESSURE_TREND_HOURS = 3L
        const val PRESSURE_MIN = 960f
        const val PRESSURE_MAX = 1060f
        const val UV_MAX = 12f
        const val AQI_MAX = 100f
        const val WIND_SCALE_KMH = 80.0
        const val HALF_TURN = 180
        const val FULL_TURN = 360
        const val SCALE_PADDING = 3.0
        const val NARROW_STEP = 5.0
        const val WIDE_STEP = 10.0
        const val WIDE_SPAN = 30.0
        const val MIN_SPAN_STEPS = 4
        val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
