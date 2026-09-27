package com.kampplus.ufuk.feature.forecast.presentation.di

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.feature.forecast.domain.policy.WeatherCondition
import com.kampplus.ufuk.feature.forecast.presentation.model.ConditionUi
import dagger.MapKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@MapKey
annotation class WeatherConditionKey(
    val value: WeatherCondition
)

@Module
@InstallIn(SingletonComponent::class)
object ConditionUiModule {
    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Clear)
    fun clear() = ConditionUi(GlyphKind.Clear, R.string.condition_clear)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.MainlyClear)
    fun mainlyClear() = ConditionUi(GlyphKind.PartlyCloudy, R.string.condition_mainly_clear)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.PartlyCloudy)
    fun partlyCloudy() = ConditionUi(GlyphKind.PartlyCloudy, R.string.condition_partly_cloudy)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Overcast)
    fun overcast() = ConditionUi(GlyphKind.Cloudy, R.string.condition_overcast)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Fog)
    fun fog() = ConditionUi(GlyphKind.Fog, R.string.condition_fog)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Drizzle)
    fun drizzle() = ConditionUi(GlyphKind.Drizzle, R.string.condition_drizzle)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.FreezingRain)
    fun freezingRain() = ConditionUi(GlyphKind.Rain, R.string.condition_freezing_rain)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Rain)
    fun rain() = ConditionUi(GlyphKind.Rain, R.string.condition_rain)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Snow)
    fun snow() = ConditionUi(GlyphKind.Snow, R.string.condition_snow)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.RainShowers)
    fun rainShowers() = ConditionUi(GlyphKind.Rain, R.string.condition_rain_showers)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.SnowShowers)
    fun snowShowers() = ConditionUi(GlyphKind.Snow, R.string.condition_snow_showers)

    @Provides @IntoMap
    @WeatherConditionKey(WeatherCondition.Thunderstorm)
    fun thunderstorm() = ConditionUi(GlyphKind.Thunderstorm, R.string.condition_thunderstorm)
}
