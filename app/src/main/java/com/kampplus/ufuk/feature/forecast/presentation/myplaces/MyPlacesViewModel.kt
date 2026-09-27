package com.kampplus.ufuk.feature.forecast.presentation.myplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.error.AppError
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.text.toUiText
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.usecase.GetPlaceConditionsUseCase
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowMapper
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowUi
import com.kampplus.ufuk.feature.places.domain.model.RemovedPlace
import com.kampplus.ufuk.feature.places.domain.usecase.LocateDeviceUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.RemoveSavedPlaceUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.RestoreSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPlacesUiState(
    val isLoading: Boolean = true,
    val deviceRow: PlaceRowUi? = null,
    val rows: List<PlaceRowUi> = emptyList(),
    val isLocating: Boolean = false,
    /** Ağ yoksa satırlar önbellekten gelir ya da boş kalır; ekran bunu tek satırla söyler. */
    val notice: UiText? = null,
    val isRefreshing: Boolean = false
)

sealed interface MyPlacesEvent {
    data class PlaceRemoved(
        val removed: RemovedPlace,
        val title: UiText
    ) : MyPlacesEvent

    data class Message(
        val text: UiText
    ) : MyPlacesEvent
}

@HiltViewModel
class MyPlacesViewModel @Inject constructor(
    observeSavedPlaces: ObserveSavedPlacesUseCase,
    private val getPlaceConditions: GetPlaceConditionsUseCase,
    observeUnitSettings: ObserveUnitSettingsUseCase,
    private val locateDevice: LocateDeviceUseCase,
    private val removeSavedPlace: RemoveSavedPlaceUseCase,
    private val restoreSavedPlace: RestoreSavedPlaceUseCase,
    private val rowMapper: PlaceRowMapper
) : ViewModel() {

    private val places: Flow<List<Place>> = observeSavedPlaces().distinctUntilChanged()
    private val conditions = MutableStateFlow(ConditionsState())
    private val reload = MutableStateFlow(0)
    private val isLocating = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private var latestPlaces: List<Place> = emptyList()

    private val _events = Channel<MyPlacesEvent>(Channel.BUFFERED)

    /** Tek seferlik UI olayları (snackbar). State'e konmaz; ekran dönünce tekrar gösterilmemeli. */
    val events: Flow<MyPlacesEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<MyPlacesUiState> = combine(
        places,
        conditions,
        observeUnitSettings(),
        isLocating,
        isRefreshing
    ) { places, conditions, units, locating, refreshing ->
        val rows = places.map { place -> rowMapper.toRow(place, conditions.byPlace[place.id], units) }
        MyPlacesUiState(
            isLoading = false,
            deviceRow = rows.firstOrNull { it.isDeviceLocation },
            rows = rows.filterNot { it.isDeviceLocation },
            isLocating = locating,
            notice = when {
                conditions.error != null && conditions.byPlace.isEmpty() && places.isNotEmpty() -> conditions.error.toUiText()
                conditions.byPlace.values.any { it.isFromCache } -> UiText.Resource(R.string.notice_offline_rows)
                else -> null
            },
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = MyPlacesUiState()
    )

    init {
        // Yer listesi değişince (ekleme, silme, konum güncelleme) hepsi tek istekte yeniden sorulur.
        // Yeni sonuç gelene kadar eski değerler ekranda kalır.
        viewModelScope.launch {
            combine(places, reload) { places, _ -> places }.collectLatest { places ->
                latestPlaces = places
                when (val result = getPlaceConditions(places)) {
                    is AppResult.Success -> conditions.value = ConditionsState(byPlace = result.data.associateBy { it.place.id })
                    is AppResult.Failure -> conditions.update { it.copy(error = result.error) }
                }
                isRefreshing.value = false
            }
        }
    }

    fun findPlace(placeId: Long): Place? = latestPlaces.firstOrNull { it.id == placeId }

    fun onRefresh() {
        isRefreshing.value = true
        reload.update { it + 1 }
    }

    fun onLocate() {
        if (isLocating.value) return
        viewModelScope.launch {
            isLocating.value = true
            val result = locateDevice()
            isLocating.value = false
            if (result is AppResult.Failure) _events.send(MyPlacesEvent.Message(result.error.toUiText()))
        }
    }

    fun onLocationPermissionDenied() {
        viewModelScope.launch { _events.send(MyPlacesEvent.Message(AppError.LocationPermissionDenied.toUiText())) }
    }

    fun onRemove(placeId: Long) {
        viewModelScope.launch {
            val removed = removeSavedPlace(placeId) ?: return@launch
            _events.send(MyPlacesEvent.PlaceRemoved(removed = removed, title = rowMapper.title(removed.place)))
        }
    }

    /** Geri alınan yer, olayla birlikte taşınır: hangi snackbar'a basıldıysa o yer geri gelir. */
    fun onUndoRemove(removed: RemovedPlace) {
        viewModelScope.launch { restoreSavedPlace(removed) }
    }

    private data class ConditionsState(
        val byPlace: Map<Long, PlaceConditions> = emptyMap(),
        val error: AppError? = null
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
