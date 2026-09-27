package com.kampplus.ufuk.core.ui.state

import com.kampplus.ufuk.core.ui.text.UiText

/** Dört ekran durumu: yükleniyor / veri var / boş / hata. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<T>(
        val data: T
    ) : UiState<T>

    data object Empty : UiState<Nothing>

    data class Error(
        val message: UiText
    ) : UiState<Nothing>
}
