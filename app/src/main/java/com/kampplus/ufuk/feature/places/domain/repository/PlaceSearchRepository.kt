package com.kampplus.ufuk.feature.places.domain.repository

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place

interface PlaceSearchRepository {
    /** Ada göre yerleşim yeri arar. Sonuç yoksa boş liste (hata değil). */
    suspend fun search(query: String): AppResult<List<Place>>

    /** Keşfet ekranında öne çıkan yerler. */
    fun featured(): List<Place>
}
