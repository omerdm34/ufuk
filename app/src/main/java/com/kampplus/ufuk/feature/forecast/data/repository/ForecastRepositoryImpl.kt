package com.kampplus.ufuk.feature.forecast.data.repository

import com.kampplus.ufuk.core.common.dispatcher.IoDispatcher
import com.kampplus.ufuk.core.common.error.ErrorMapper
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.common.result.runCatchingApp
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.data.local.CachedPayload
import com.kampplus.ufuk.feature.forecast.data.local.ForecastLocalDataSource
import com.kampplus.ufuk.feature.forecast.data.mapper.toConditions
import com.kampplus.ufuk.feature.forecast.data.mapper.toDomain
import com.kampplus.ufuk.feature.forecast.data.remote.ForecastRemoteDataSource
import com.kampplus.ufuk.feature.forecast.domain.model.ForecastSnapshot
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.repository.ForecastRepository
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ForecastRepositoryImpl @Inject constructor(
    private val remoteDataSource: ForecastRemoteDataSource,
    private val localDataSource: ForecastLocalDataSource,
    private val errorMapper: ErrorMapper,
    private val clock: Clock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ForecastRepository {

    override fun observeForecast(place: Place): Flow<ForecastSnapshot?> = localDataSource.observe(place.coordinates.cacheKey)
        .map { cached -> cached?.toSnapshotOrNull() }
        .flowOn(ioDispatcher)

    /** Yanıt önce domain modeline çevrilerek doğrulanır; bozuk yanıt önbelleğe hiç girmez. */
    override suspend fun refreshForecast(place: Place): AppResult<Unit> = withContext(ioDispatcher) {
        errorMapper.runCatchingApp {
            val payload = remoteDataSource.fetchForecast(place.coordinates)
            payload.toDomain()
            localDataSource.save(place.coordinates.cacheKey, payload, clock.instant())
        }
    }

    override suspend fun getConditions(places: List<Place>): AppResult<List<PlaceConditions>> = withContext(ioDispatcher) {
        val remote = errorMapper.runCatchingApp {
            val fetchedAt = clock.instant()
            val responses = remoteDataSource.fetchConditions(places.map { it.coordinates })
            places.zip(responses) { place, response -> response.toConditions(place, fetchedAt) }
        }
        when (remote) {
            is AppResult.Success -> remote
            is AppResult.Failure -> {
                val cached = places.mapNotNull { place ->
                    val snapshot = localDataSource.get(place.coordinates.cacheKey)?.toSnapshotOrNull() ?: return@mapNotNull null
                    snapshot.forecast.toConditions(place, snapshot.fetchedAt)
                }
                if (cached.isEmpty()) remote else AppResult.Success(cached)
            }
        }
    }

    /** Önbellek biçimi eski bir sürümden kalmışsa ve çevrilemiyorsa yokmuş gibi davranılır. */
    private fun CachedPayload.toSnapshotOrNull(): ForecastSnapshot? = runCatching {
        ForecastSnapshot(forecast = payload.toDomain(), fetchedAt = fetchedAt)
    }.getOrNull()
}
