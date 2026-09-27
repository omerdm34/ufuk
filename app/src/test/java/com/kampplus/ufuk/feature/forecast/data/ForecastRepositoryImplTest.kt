package com.kampplus.ufuk.feature.forecast.data

import app.cash.turbine.test
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.network.error.NetworkErrorMapper
import com.kampplus.ufuk.feature.forecast.data.local.CachedPayload
import com.kampplus.ufuk.feature.forecast.data.local.ForecastLocalDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.ForecastRemoteDataSource
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastPayload
import com.kampplus.ufuk.feature.forecast.data.remote.dto.ForecastResponseDto
import com.kampplus.ufuk.feature.forecast.data.repository.ForecastRepositoryImpl
import com.kampplus.ufuk.testing.ankara
import com.kampplus.ufuk.testing.ankaraPayload
import com.kampplus.ufuk.testing.istanbul
import com.kampplus.ufuk.testing.readResource
import com.kampplus.ufuk.testing.testJson
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastRepositoryImplTest {

    private val now = Instant.parse("2026-09-27T11:15:00Z")
    private val remote = FakeRemote()
    private val local = InMemoryLocal()
    private val repository = ForecastRepositoryImpl(
        remoteDataSource = remote,
        localDataSource = local,
        errorMapper = NetworkErrorMapper(),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        ioDispatcher = UnconfinedTestDispatcher()
    )

    @Test
    fun `refresh writes the cache that the screen observes`() = runTest {
        repository.observeForecast(ankara).test {
            assertNull(awaitItem())

            assertEquals(AppResult.Success(Unit), repository.refreshForecast(ankara))

            val snapshot = awaitItem()!!
            assertEquals(22.4, snapshot.forecast.current.temperatureC, 0.0)
            assertEquals(now, snapshot.fetchedAt)
        }
    }

    @Test
    fun `failed refresh keeps the previous forecast`() = runTest {
        repository.refreshForecast(ankara)
        remote.failWith = IOException("offline")

        val result = repository.refreshForecast(ankara)

        assertEquals(AppResult.Failure(AppError.Network), result)
        assertEquals(now, local.get(ankara.coordinates.cacheKey)!!.fetchedAt)
    }

    @Test
    fun `malformed response never reaches the cache`() = runTest {
        remote.payload = ForecastPayload(forecast = ForecastResponseDto(latitude = 39.9, longitude = 32.8))

        val result = repository.refreshForecast(ankara)

        assertEquals(AppResult.Failure(AppError.Parse), result)
        assertNull(local.get(ankara.coordinates.cacheKey))
    }

    @Test
    fun `conditions come from one network call in place order`() = runTest {
        val result = repository.getConditions(listOf(istanbul, ankara)) as AppResult.Success

        assertEquals(listOf("İstanbul", "Ankara"), result.data.map { it.place.name })
        assertEquals(12.0, result.data.last().todayMinC!!, 0.0)
        assertTrue(result.data.none { it.isFromCache })
    }

    @Test
    fun `offline conditions fall back to cached forecasts`() = runTest {
        repository.refreshForecast(ankara)
        remote.failWith = IOException("offline")

        val result = repository.getConditions(listOf(istanbul, ankara)) as AppResult.Success

        val row = result.data.single()
        assertEquals("Ankara", row.place.name)
        assertTrue(row.isFromCache)
        assertEquals(23.5, row.todayMaxC!!, 0.0)
    }

    @Test
    fun `offline with nothing cached is an error`() = runTest {
        remote.failWith = IOException("offline")

        assertEquals(AppResult.Failure(AppError.Network), repository.getConditions(listOf(istanbul)))
    }

    private class FakeRemote : ForecastRemoteDataSource {
        var failWith: Exception? = null
        var payload: ForecastPayload = ankaraPayload()

        override suspend fun fetchForecast(coordinates: Coordinates): ForecastPayload {
            failWith?.let { throw it }
            return payload
        }

        override suspend fun fetchConditions(coordinates: List<Coordinates>): List<ForecastResponseDto> {
            failWith?.let { throw it }
            return testJson.decodeFromString(ListSerializer(ForecastResponseDto.serializer()), readResource("conditions_multi.json"))
        }
    }

    private class InMemoryLocal : ForecastLocalDataSource {
        private val entries = MutableStateFlow<Map<String, CachedPayload>>(emptyMap())

        override fun observe(cacheKey: String): Flow<CachedPayload?> = entries.map { it[cacheKey] }

        override suspend fun get(cacheKey: String): CachedPayload? = entries.value[cacheKey]

        override suspend fun save(cacheKey: String, payload: ForecastPayload, fetchedAt: Instant) {
            entries.value += cacheKey to CachedPayload(payload, fetchedAt)
        }
    }
}
