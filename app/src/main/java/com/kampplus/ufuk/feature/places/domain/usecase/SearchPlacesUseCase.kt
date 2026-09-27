package com.kampplus.ufuk.feature.places.domain.usecase

import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.places.domain.repository.PlaceSearchRepository
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val repository: PlaceSearchRepository
) {
    /** [MIN_QUERY_LENGTH] harften kısa sorgu için servise gidilmez. */
    suspend operator fun invoke(query: String): AppResult<List<Place>> {
        val text = query.trim()
        return if (text.length < MIN_QUERY_LENGTH) AppResult.Success(emptyList()) else repository.search(text)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
