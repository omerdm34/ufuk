package com.kampplus.ufuk.feature.places

import app.cash.turbine.test
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.usecase.GetPlaceConditionsUseCase
import com.kampplus.ufuk.feature.forecast.presentation.explore.ExploreSort
import com.kampplus.ufuk.feature.forecast.presentation.explore.ExploreUiState
import com.kampplus.ufuk.feature.forecast.presentation.explore.ExploreViewModel
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowMapper
import com.kampplus.ufuk.feature.places.domain.usecase.GetFeaturedPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlaceIdsUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.SearchPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ToggleSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import com.kampplus.ufuk.testing.FakeForecastRepository
import com.kampplus.ufuk.testing.FakePlaceSearchRepository
import com.kampplus.ufuk.testing.FakeSavedPlaceRepository
import com.kampplus.ufuk.testing.FakeSettingsRepository
import com.kampplus.ufuk.testing.MainDispatcherRule
import com.kampplus.ufuk.testing.ankara
import com.kampplus.ufuk.testing.conditionsFor
import com.kampplus.ufuk.testing.testConditions
import com.kampplus.ufuk.testing.testForecastUiMapper
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExploreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saved = FakeSavedPlaceRepository(listOf(ankara))
    private val search = FakePlaceSearchRepository()
    private val forecast = FakeForecastRepository(conditionsResult = { places -> AppResult.Success(places.map { conditionsFor(it) }) })

    private fun createViewModel() = ExploreViewModel(
        getFeaturedPlaces = GetFeaturedPlacesUseCase(search),
        searchPlaces = SearchPlacesUseCase(search),
        getPlaceConditions = GetPlaceConditionsUseCase(forecast),
        observeSavedPlaceIds = ObserveSavedPlaceIdsUseCase(saved),
        toggleSavedPlace = ToggleSavedPlaceUseCase(saved),
        observeUnitSettings = ObserveUnitSettingsUseCase(FakeSettingsRepository()),
        rowMapper = PlaceRowMapper(testConditions, testForecastUiMapper())
    )

    private fun ExploreUiState.titles() = (content as UiState.Success).data.map { (it.title as UiText.Dynamic).value }

    @Test
    fun `featured cities are listed alphabetically with the extremes`() = runTest {
        createViewModel().uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(listOf("Ankara", "İstanbul", "İzmir"), state.titles())
            assertEquals(UiText.Resource(R.string.explore_extremes, "İzmir", "26°", "İstanbul", "19°"), state.extremes)
            assertTrue((state.content as UiState.Success).data.first().isSaved)
        }
    }

    @Test
    fun `sorting by warmth reorders the featured list`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            viewModel.onSortChange(ExploreSort.Warmest)
            advanceUntilIdle()
            assertEquals(listOf("İzmir", "Ankara", "İstanbul"), expectMostRecentItem().titles())
        }
    }

    @Test
    fun `typing waits for a pause before searching`() = runTest {
        val rize = Place(740483, "Rize", "Rize", "Türkiye", Coordinates(41.02, 40.52))
        search.searchResult = { AppResult.Success(listOf(rize)) }
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onQueryChange("Ri")
            advanceTimeBy(100)
            viewModel.onQueryChange("Rize")
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(listOf("Rize"), state.titles())
            assertEquals(null, state.extremes)
        }
        assertEquals(listOf("Rize"), search.queries)
    }

    @Test
    fun `no match is the empty state and a failure offers retry`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onQueryChange("Xyzzy")
            advanceUntilIdle()
            assertEquals(UiState.Empty, expectMostRecentItem().content)

            search.searchResult = { AppResult.Failure(AppError.Network) }
            viewModel.onRetry()
            advanceUntilIdle()
            assertEquals(UiState.Error(UiText.Resource(R.string.error_network)), expectMostRecentItem().content)
        }
    }

    @Test
    fun `the star saves and unsaves a place`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()
            viewModel.onToggleSaved(ankara.id)
            advanceUntilIdle()
            assertTrue((expectMostRecentItem().content as UiState.Success).data.none { it.isSaved })
        }
        assertEquals(emptyList<String>(), saved.savedNames())
    }
}
