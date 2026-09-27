package com.kampplus.ufuk.feature.forecast.presentation.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.ufuk.core.model.Place

/** ViewModel'i ekrana bağlayan katman. Ekranın kendisi ([ExploreScreen]) stateless'tır. */
@Composable
fun ExploreRoute(onPlaceClick: (Place) -> Unit, modifier: Modifier = Modifier, viewModel: ExploreViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExploreScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onPlaceClick = { id -> viewModel.findPlace(id)?.let(onPlaceClick) },
        onToggleSaved = viewModel::onToggleSaved,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        modifier = modifier
    )
}
