package com.kampplus.ufuk.core.ui.text

import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError

/** Hata sözlüğünün kullanıcıya gösterilecek karşılığı: sorunu ve çözümü söyler. */
fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.Resource(R.string.error_network)
    AppError.NotFound -> UiText.Resource(R.string.error_not_found)
    AppError.Parse -> UiText.Resource(R.string.error_parse)
    AppError.LocationUnavailable -> UiText.Resource(R.string.error_location_unavailable)
    AppError.LocationPermissionDenied -> UiText.Resource(R.string.error_location_permission)
    is AppError.Server -> UiText.Resource(R.string.error_server, code)
    is AppError.Unknown -> UiText.Resource(R.string.error_generic)
}
