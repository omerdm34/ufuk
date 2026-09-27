package com.kampplus.ufuk.feature.places

import app.cash.turbine.test
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.feature.forecast.domain.usecase.GetPlaceConditionsUseCase
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowMapper
import com.kampplus.ufuk.feature.forecast.presentation.myplaces.MyPlacesEvent
import com.kampplus.ufuk.feature.forecast.presentation.myplaces.MyPlacesViewModel
import com.kampplus.ufuk.feature.places.domain.usecase.LocateDeviceUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.RemoveSavedPlaceUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.RestoreSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import com.kampplus.ufuk.testing.FakeForecastRepository
import com.kampplus.ufuk.testing.FakeLocationRepository
import com.kampplus.ufuk.testing.FakeSavedPlaceRepository
import com.kampplus.ufuk.testing.FakeSettingsRepository
import com.kampplus.ufuk.testing.MainDispatcherRule
import com.kampplus.ufuk.testing.ankara
import com.kampplus.ufuk.testing.conditionsFor
import com.kampplus.ufuk.testing.istanbul
import com.kampplus.ufuk.testing.izmir
import com.kampplus.ufuk.testing.testConditions
import com.kampplus.ufuk.testing.testForecastUiMapper
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MyPlacesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saved = FakeSavedPlaceRepository(listOf(istanbul, ankara, izmir))
    private val forecast = FakeForecastRepository(conditionsResult = { places -> AppResult.Success(places.map { conditionsFor(it) }) })
    private val location = FakeLocationRepository()

    private fun createViewModel() = MyPlacesViewModel(
        observeSavedPlaces = ObserveSavedPlacesUseCase(saved),
        getPlaceConditions = GetPlaceConditionsUseCase(forecast),
        observeUnitSettings = ObserveUnitSettingsUseCase(FakeSettingsRepository()),
        locateDevice = LocateDeviceUseCase(location, saved),
        removeSavedPlace = RemoveSavedPlaceUseCase(saved),
        restoreSavedPlace = RestoreSavedPlaceUseCase(saved),
        rowMapper = PlaceRowMapper(testConditions, testForecastUiMapper())
    )

    @Test
    fun `saved places come with their weather from one request`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(listOf("19°", "22°", "26°"), state.rows.map { it.temperatureText })
            assertNull(state.deviceRow)
        }
        assertEquals(1, forecast.conditionRequests.size)
    }

    @Test
    fun `undo restores the place of that snackbar even after a second removal`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onRemove(istanbul.id)
            val first = awaitItem() as MyPlacesEvent.PlaceRemoved
            viewModel.onRemove(izmir.id)
            awaitItem()

            viewModel.onUndoRemove(first.removed)
            advanceUntilIdle()
        }

        assertEquals(listOf("İstanbul", "Ankara"), saved.savedNames())
    }

    @Test
    fun `places stay listed when the network is down`() = runTest {
        forecast.conditionsResult = { AppResult.Failure(AppError.Network) }
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(3, state.rows.size)
            assertNull(state.rows.first().temperatureText)
            assertEquals(UiText.Resource(R.string.error_network), state.notice)
        }
    }

    @Test
    fun `cached rows are announced as offline`() = runTest {
        forecast.conditionsResult = { places -> AppResult.Success(places.map { conditionsFor(it, fromCache = true) }) }
        val viewModel = createViewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(UiText.Resource(R.string.notice_offline_rows), expectMostRecentItem().notice)
        }
    }

    @Test
    fun `located device becomes the first row`() = runTest {
        location.result = AppResult.Success(Place(Place.DEVICE_LOCATION_ID, "Çankaya", "Ankara", "Türkiye", ankara.coordinates))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            viewModel.onLocate()
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNotNull(state.deviceRow)
            assertEquals(UiText.Dynamic("Çankaya"), state.deviceRow!!.title)
            assertEquals(3, state.rows.size)
        }
    }

    @Test
    fun `location failure is explained in a message`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onLocate()
            assertEquals(MyPlacesEvent.Message(UiText.Resource(R.string.error_location_unavailable)), awaitItem())
        }
    }
}
