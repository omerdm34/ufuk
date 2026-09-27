package com.kampplus.ufuk.testing

import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.data.mapper.toDomain
import com.kampplus.ufuk.feature.forecast.domain.model.ForecastSnapshot
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Önbelleği bellekte tutan sahte repository; yenileme sonucu testten ayarlanır. */
class FakeForecastRepository(
    var fetchedAt: Instant = Instant.parse("2026-09-27T11:15:00Z"),
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit),
    var conditionsResult: (List<Place>) -> AppResult<List<PlaceConditions>> = { AppResult.Failure(AppError.Network) }
) : ForecastRepository {

    private val cache = MutableStateFlow<Map<String, ForecastSnapshot>>(emptyMap())
    var refreshCount = 0
        private set
    val conditionRequests = mutableListOf<List<Place>>()

    fun seed(place: Place, snapshot: ForecastSnapshot = ForecastSnapshot(ankaraPayload().toDomain(), fetchedAt)) {
        cache.value += place.coordinates.cacheKey to snapshot
    }

    override fun observeForecast(place: Place): Flow<ForecastSnapshot?> = cache.map { it[place.coordinates.cacheKey] }

    override suspend fun refreshForecast(place: Place): AppResult<Unit> {
        refreshCount++
        if (refreshResult is AppResult.Success) seed(place)
        return refreshResult
    }

    override suspend fun getConditions(places: List<Place>): AppResult<List<PlaceConditions>> {
        conditionRequests += places
        return conditionsResult(places)
    }
}
