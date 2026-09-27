package com.kampplus.ufuk.feature.forecast.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.common.time.Ticker
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.navigation.ForecastDestination
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.policy.FreshnessPolicy
import com.kampplus.ufuk.feature.forecast.domain.usecase.ObserveForecastUseCase
import com.kampplus.ufuk.feature.forecast.domain.usecase.RefreshForecastUseCase
import com.kampplus.ufuk.feature.forecast.presentation.detail.ForecastDetailUiState
import com.kampplus.ufuk.feature.forecast.presentation.detail.ForecastDetailViewModel
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlaceIdsUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ToggleSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import com.kampplus.ufuk.testing.FakeForecastRepository
import com.kampplus.ufuk.testing.FakeSavedPlaceRepository
import com.kampplus.ufuk.testing.FakeSettingsRepository
import com.kampplus.ufuk.testing.MainDispatcherRule
import com.kampplus.ufuk.testing.ankara
import com.kampplus.ufuk.testing.testForecastUiMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForecastDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fetchedAt = Instant.parse("2026-09-27T11:15:00Z")
    private var now = fetchedAt.plusSeconds(3 * 60)
    private val repository = FakeForecastRepository(fetchedAt = fetchedAt)
    private val settings = FakeSettingsRepository()
    private val saved = FakeSavedPlaceRepository()

    private fun createViewModel() = ForecastDetailViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                ForecastDestination.ARG_PLACE_ID to ankara.id,
                ForecastDestination.ARG_NAME to ankara.name,
                ForecastDestination.ARG_REGION to ankara.region,
                ForecastDestination.ARG_COUNTRY to ankara.country,
                ForecastDestination.ARG_LATITUDE to ankara.coordinates.latitude,
                ForecastDestination.ARG_LONGITUDE to ankara.coordinates.longitude
            )
        ),
        observeForecast = ObserveForecastUseCase(repository),
        refreshForecast = RefreshForecastUseCase(repository),
        observeUnitSettings = ObserveUnitSettingsUseCase(settings),
        freshnessPolicy = FreshnessPolicy(),
        uiMapper = testForecastUiMapper(),
        observeSavedPlaceIds = ObserveSavedPlaceIdsUseCase(saved),
        toggleSavedPlace = ToggleSavedPlaceUseCase(saved),
        clock = Clock.fixed(now, ZoneOffset.UTC),
        ticker = Ticker { flowOf(now) }
    )

    @Test
    fun `first visit loads from the network into the cache`() = runTest {
        createViewModel().uiState.test {
            assertEquals(UiState.Loading, awaitItem().content)
            val state = awaitItem()
            assertTrue(state.content is UiState.Success)
            assertEquals(UiText.Resource(R.string.freshness_updated, UiText.Plural(R.plurals.age_minutes, 3, 3)), state.freshness!!.text)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `a recent cache is shown without a network call`() = runTest {
        repository.seed(ankara)

        createViewModel().uiState.test {
            assertTrue(expectMostRecentItemAfterIdle().content is UiState.Success)
        }
        assertEquals(0, repository.refreshCount)
    }

    @Test
    fun `failure without a cache shows the error with retry`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Network)

        val viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(UiState.Loading, awaitItem().content)
            assertEquals(UiState.Error(UiText.Resource(R.string.error_network)), awaitItem().content)

            repository.refreshResult = AppResult.Success(Unit)
            viewModel.onRetry()

            assertTrue(expectMostRecentItemAfterIdle().content is UiState.Success)
        }
    }

    @Test
    fun `failed refresh keeps the old forecast and says it is stale`() = runTest {
        repository.seed(ankara)
        now = fetchedAt.plusSeconds(50 * 60)
        repository.refreshResult = AppResult.Failure(AppError.Network)

        createViewModel().uiState.test {
            val state = expectMostRecentItemAfterIdle()
            assertTrue(state.content is UiState.Success)
            assertTrue(state.freshness!!.isStale)
            assertEquals(UiText.Resource(R.string.freshness_failed, UiText.Plural(R.plurals.age_minutes, 50, 50)), state.freshness!!.text)
            assertFalse(state.isRefreshing)
        }
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `changing units updates the open screen`() = runTest {
        repository.seed(ankara)

        createViewModel().uiState.test {
            assertEquals("22°", successModel(expectMostRecentItemAfterIdle()).dial.nowText)

            settings.setTemperatureUnit(TemperatureUnit.Fahrenheit)

            assertEquals("72°", successModel(awaitItem()).dial.nowText)
        }
    }

    private fun ReceiveTurbine<ForecastDetailUiState>.expectMostRecentItemAfterIdle(): ForecastDetailUiState {
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        return expectMostRecentItem()
    }

    private fun successModel(state: ForecastDetailUiState) = (state.content as UiState.Success).data

    @Test
    fun `the star saves the place shown on the screen`() = runTest {
        repository.seed(ankara)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(false, expectMostRecentItemAfterIdle().isSaved)
            viewModel.onToggleSaved()
            assertEquals(true, expectMostRecentItemAfterIdle().isSaved)
        }
        assertEquals(listOf("Ankara"), saved.savedNames())
    }
}
