package com.kampplus.ufuk.feature.forecast.presentation.model

import androidx.annotation.StringRes
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.model.WeatherCode
import com.kampplus.ufuk.feature.forecast.domain.policy.WeatherCondition
import com.kampplus.ufuk.feature.forecast.domain.policy.WeatherConditionClassifier
import javax.inject.Inject

/** Bir hava koşulunun ekrandaki temsili: çizilecek ikon ve adı. */
data class ConditionUi(
    val glyph: GlyphKind,
    @param:StringRes val labelRes: Int
) {
    val label: UiText get() = UiText.Resource(labelRes)

    companion object {
        val Unknown = ConditionUi(glyph = GlyphKind.Unknown, labelRes = R.string.condition_unknown)
    }
}

/**
 * Hava koşullarının UI karşılıkları Hilt `@IntoMap` ile toplanır. Yeni bir görünüm eklemek için
 * yalnızca `ConditionUiModule`'e girdi eklenir; eşlemesi olmayan koşul [ConditionUi.Unknown] ile gösterilir.
 */
class ConditionUiRegistry @Inject constructor(
    private val classifier: WeatherConditionClassifier,
    private val entries: Map<WeatherCondition, @JvmSuppressWildcards ConditionUi>
) {
    fun resolve(condition: WeatherCondition): ConditionUi = entries[condition] ?: ConditionUi.Unknown

    fun resolve(code: WeatherCode): ConditionUi = resolve(classifier.classify(code))
}
