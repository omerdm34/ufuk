package com.kampplus.ufuk.feature.places.domain.model

import com.kampplus.ufuk.core.model.Place

/**
 * Silinen yer ve listedeki eski yeri. "Geri al" bu nesneyi taşır; böylece art arda iki silmede
 * her snackbar kendi yerini geri getirir (taslak uygulamadaki tek `lastRemoved` hatası burada yok).
 */
data class RemovedPlace(
    val place: Place,
    val position: Int
)
