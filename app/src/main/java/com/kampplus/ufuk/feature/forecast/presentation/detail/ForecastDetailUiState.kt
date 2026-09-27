package com.kampplus.ufuk.feature.forecast.presentation.detail

import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.presentation.model.ForecastUiModel

data class ForecastDetailUiState(
    /** Yer adı: tahmin gelmeden de üst çubukta görünür. */
    val title: UiText,
    val content: UiState<ForecastUiModel> = UiState.Loading,
    val freshness: FreshnessUi? = null,
    /** Aşağı çekerek yenileme göstergesi; ilk yükleme ve arka plan yenilemesinde görünmez. */
    val isRefreshing: Boolean = false,
    /** null: bu yer kaydedilemez (ör. yerler özelliği bağlı değil). */
    val isSaved: Boolean? = null
)

/** Verinin yaşı. [isStale] ise ekran bunu açıkça söyler ve ibre soluklaşır. */
data class FreshnessUi(
    val text: UiText,
    val isStale: Boolean
)
