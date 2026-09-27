package com.kampplus.ufuk.feature.forecast.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.common.result.AppResult
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.ui.format.UnitFormatter
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.text.toUiText
import com.kampplus.ufuk.feature.forecast.domain.model.PlaceConditions
import com.kampplus.ufuk.feature.forecast.domain.usecase.GetPlaceConditionsUseCase
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowMapper
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowUi
import com.kampplus.ufuk.feature.places.domain.usecase.GetFeaturedPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ObserveSavedPlaceIdsUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.SearchPlacesUseCase
import com.kampplus.ufuk.feature.places.domain.usecase.ToggleSavedPlaceUseCase
import com.kampplus.ufuk.feature.settings.domain.usecase.ObserveUnitSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExploreSort {
    Alphabetical,
    Warmest,
    Coldest
}

data class ExploreUiState(
    val query: String = "",
    val sort: ExploreSort = ExploreSort.Alphabetical,
    val content: UiState<List<PlaceRowUi>> = UiState.Loading,
    /** "En sıcak Adana 31° · En serin Erzurum 11°" — yalnızca öne çıkan liste gösterilirken. */
    val extremes: UiText? = null,
    val isRefreshing: Boolean = false
) {
    /** Arama kutusu dolu mu? (Boş durum metni ve sıralama buna göre değişir.) */
    val isSearching: Boolean get() = query.trim().length >= SearchPlacesUseCase.MIN_QUERY_LENGTH
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getFeaturedPlaces: GetFeaturedPlacesUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val getPlaceConditions: GetPlaceConditionsUseCase,
    observeSavedPlaceIds: ObserveSavedPlaceIdsUseCase,
    private val toggleSavedPlace: ToggleSavedPlaceUseCase,
    observeUnitSettings: ObserveUnitSettingsUseCase,
    private val rowMapper: PlaceRowMapper
) : ViewModel() {

    /** Detaya giderken ve kaydederken yerin tamamı gerekir; son yüklenen liste burada tutulur. */
    private var loadedPlaces: Map<Long, Place> = emptyMap()

    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(ExploreSort.Alphabetical)
    private val reloadTrigger = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)

    /**
     * Yazarken her tuşa istek atılmaz: arama [SEARCH_DEBOUNCE_MS] bekletilir. Kısa sorguda öne çıkan
     * şehirler gösterilir. flatMapLatest, yeni sorgu gelince eski isteği iptal eder.
     */
    private val results: Flow<SearchResult?> = combine(
        query
            .map { it.trim().takeIf { text -> text.length >= SearchPlacesUseCase.MIN_QUERY_LENGTH }.orEmpty() }
            .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
            .distinctUntilChanged(),
        reloadTrigger
    ) { searchText, _ -> searchText }
        .flatMapLatest { searchText ->
            flow {
                if (!isRefreshing.value) emit(null)
                emit(load(searchText))
                isRefreshing.value = false
            }
        }

    val uiState: StateFlow<ExploreUiState> = combine(
        combine(query, sort, ::Pair),
        results,
        observeSavedPlaceIds(),
        observeUnitSettings(),
        isRefreshing
    ) { (query, sort), result, savedIds, units, refreshing ->
        ExploreUiState(
            query = query,
            sort = sort,
            content = when (result) {
                null -> UiState.Loading
                is SearchResult.Failed -> UiState.Error(result.message)
                is SearchResult.Loaded -> if (result.places.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(rows(result, sort, savedIds, units))
                }
            },
            extremes = (result as? SearchResult.Loaded)?.takeIf { it.isFeatured }?.let { extremes(it.conditions, units) },
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ExploreUiState()
    )

    fun findPlace(placeId: Long): Place? = loadedPlaces[placeId]

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun onSortChange(value: ExploreSort) {
        sort.value = value
    }

    fun onRetry() {
        reloadTrigger.update { it + 1 }
    }

    fun onRefresh() {
        isRefreshing.value = true
        reloadTrigger.update { it + 1 }
    }

    fun onToggleSaved(placeId: Long) {
        val place = loadedPlaces[placeId] ?: return
        viewModelScope.launch { toggleSavedPlace(place) }
    }

    private suspend fun load(searchText: String): SearchResult {
        val isFeatured = searchText.isEmpty()
        val places = if (isFeatured) {
            getFeaturedPlaces()
        } else {
            when (val found = searchPlaces(searchText)) {
                is AppResult.Failure -> return SearchResult.Failed(found.error.toUiText())
                is AppResult.Success -> found.data
            }
        }
        loadedPlaces = places.associateBy { it.id }
        if (places.isEmpty()) return SearchResult.Loaded(places, emptyMap(), isFeatured)
        return when (val weather = getPlaceConditions(places)) {
            is AppResult.Failure -> SearchResult.Failed(weather.error.toUiText())
            is AppResult.Success -> SearchResult.Loaded(places, weather.data.associateBy { it.place.id }, isFeatured)
        }
    }

    /** Arama sonuçları alaka sırasında kalır; sıralama yalnızca öne çıkan listeye uygulanır. */
    private fun rows(result: SearchResult.Loaded, sort: ExploreSort, savedIds: Set<Long>, units: UnitSettings): List<PlaceRowUi> {
        val collator = Collator.getInstance(Locale.getDefault())
        val ordered = if (!result.isFeatured) {
            result.places
        } else {
            when (sort) {
                ExploreSort.Alphabetical -> result.places.sortedWith(compareBy(collator) { it.name })
                ExploreSort.Warmest -> result.places.sortedByDescending {
                    result.conditions[it.id]?.temperatureC ?: Double.NEGATIVE_INFINITY
                }
                ExploreSort.Coldest -> result.places.sortedBy { result.conditions[it.id]?.temperatureC ?: Double.POSITIVE_INFINITY }
            }
        }
        return ordered.map { place ->
            rowMapper.toRow(place, result.conditions[place.id], units, isSaved = place.id in savedIds, showRegion = !result.isFeatured)
        }
    }

    private fun extremes(conditions: Map<Long, PlaceConditions>, units: UnitSettings): UiText? {
        val warmest = conditions.values.maxByOrNull { it.temperatureC } ?: return null
        val coldest = conditions.values.minByOrNull { it.temperatureC } ?: return null
        val format = UnitFormatter(units)
        return UiText.Resource(
            R.string.explore_extremes,
            warmest.place.name,
            format.temperature(warmest.temperatureC),
            coldest.place.name,
            format.temperature(coldest.temperatureC)
        )
    }

    private sealed interface SearchResult {
        data class Loaded(
            val places: List<Place>,
            val conditions: Map<Long, PlaceConditions>,
            val isFeatured: Boolean
        ) : SearchResult

        data class Failed(
            val message: UiText
        ) : SearchResult
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
