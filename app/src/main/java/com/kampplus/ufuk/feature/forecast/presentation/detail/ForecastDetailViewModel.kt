package com.kampplus.ufuk.feature.forecast.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.common.time.Ticker
import com.kampplus.ufuk.core.model.Coordinates
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.navigation.ForecastDestination
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.text.toUiText
import com.kampplus.ufuk.feature.forecast.domain.model.ForecastSnapshot
import com.kampplus.ufuk.feature.forecast.domain.policy.FreshnessPolicy
import com.kampplus.ufuk.feature.forecast.domain.usecase.ObserveForecastUseCase
import com.kampplus.ufuk.feature.forecast.domain.usecase.RefreshForecastUseCase
import com.kampplus.ufuk.feature.forecast.presentation.model.ForecastUiMapper
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlaceIdsUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ToggleSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Ekran her zaman önbelleği izler; ağ yalnızca önbelleği tazeler. Böylece yenileme başarısız
 * olsa da son tahmin ekranda kalır, yalnızca yaşı "güncellenemedi" notuyla söylenir.
 */
@HiltViewModel
class ForecastDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeForecast: ObserveForecastUseCase,
    private val refreshForecast: RefreshForecastUseCase,
    observeUnitSettings: ObserveUnitSettingsUseCase,
    private val freshnessPolicy: FreshnessPolicy,
    private val uiMapper: ForecastUiMapper,
    observeSavedPlaceIds: ObserveSavedPlaceIdsUseCase,
    private val toggleSavedPlace: ToggleSavedPlaceUseCase,
    private val clock: Clock,
    ticker: Ticker
) : ViewModel() {

    // toRoute<ForecastDestination>() Android Bundle'a ihtiyaç duyar; anahtarla okumak JVM testlerini sade tutar.
    val place = Place(
        id = checkNotNull(savedStateHandle[ForecastDestination.ARG_PLACE_ID]),
        name = checkNotNull(savedStateHandle[ForecastDestination.ARG_NAME]),
        region = savedStateHandle[ForecastDestination.ARG_REGION],
        country = savedStateHandle[ForecastDestination.ARG_COUNTRY],
        coordinates = Coordinates(
            latitude = checkNotNull(savedStateHandle[ForecastDestination.ARG_LATITUDE]),
            longitude = checkNotNull(savedStateHandle[ForecastDestination.ARG_LONGITUDE])
        )
    )

    private val snapshots = observeForecast(place)
    private val refresh = MutableStateFlow(RefreshStatus())

    val uiState: StateFlow<ForecastDetailUiState> = combine(
        snapshots,
        observeUnitSettings(),
        refresh,
        ticker.ticks(),
        observeSavedPlaceIds()
    ) { snapshot, units, status, now, savedIds ->
        ForecastDetailUiState(
            title = uiMapper.placeTitle(place),
            content = when {
                snapshot != null -> UiState.Success(uiMapper.toUiModel(place, snapshot.forecast, units))
                status.error != null && !status.inFlight -> UiState.Error(status.error.toUiText())
                else -> UiState.Loading
            },
            freshness = snapshot?.let { freshness(it, now, status.error) },
            isRefreshing = status.inFlight && status.showIndicator && snapshot != null,
            // Cihaz konumu zaten "Yerlerim"in başında; yıldızla kaydedilmez.
            isSaved = if (place.isDeviceLocation) null else place.id in savedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ForecastDetailUiState(title = uiMapper.placeTitle(place))
    )

    init {
        viewModelScope.launch {
            val cached = snapshots.first()
            if (freshnessPolicy.shouldRefresh(cached?.fetchedAt, clock.instant())) refresh(showIndicator = false)
        }
    }

    fun onRefresh() {
        viewModelScope.launch { refresh(showIndicator = true) }
    }

    fun onToggleSaved() {
        viewModelScope.launch { toggleSavedPlace(place) }
    }

    fun onRetry() {
        viewModelScope.launch { refresh(showIndicator = false) }
    }

    private suspend fun refresh(showIndicator: Boolean) {
        if (refresh.value.inFlight) return
        refresh.update { it.copy(inFlight = true, showIndicator = showIndicator) }
        val result = refreshForecast(place)
        refresh.value = RefreshStatus(error = (result as? AppResult.Failure)?.error)
    }

    private fun freshness(snapshot: ForecastSnapshot, now: Instant, error: AppError?): FreshnessUi {
        val age = freshnessPolicy.age(snapshot.fetchedAt, now)
        val minutes = age.toMinutes().toInt()
        val ageText = if (minutes < MINUTES_PER_HOUR) {
            UiText.Plural(R.plurals.age_minutes, minutes, minutes)
        } else {
            UiText.Plural(R.plurals.age_hours, minutes / MINUTES_PER_HOUR, minutes / MINUTES_PER_HOUR)
        }
        return when {
            error != null -> FreshnessUi(UiText.Resource(R.string.freshness_failed, ageText), isStale = true)
            freshnessPolicy.isStale(
                snapshot.fetchedAt,
                now
            ) -> FreshnessUi(UiText.Resource(R.string.freshness_stale, ageText), isStale = true)
            minutes < 1 -> FreshnessUi(UiText.Resource(R.string.freshness_just_now), isStale = false)
            else -> FreshnessUi(UiText.Resource(R.string.freshness_updated, ageText), isStale = false)
        }
    }

    private data class RefreshStatus(
        val inFlight: Boolean = false,
        val showIndicator: Boolean = false,
        val error: AppError? = null
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val MINUTES_PER_HOUR = 60
    }
}
