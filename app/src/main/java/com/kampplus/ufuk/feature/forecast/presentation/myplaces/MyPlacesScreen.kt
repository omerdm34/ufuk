package com.kampplus.ufuk.feature.forecast.presentation.myplaces

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.component.FreshnessNote
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.component.LoadingView
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.theme.UfukTheme
import com.kampplus.ufuk.feature.forecast.presentation.component.PlaceRow
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlacesScreen(
    uiState: MyPlacesUiState,
    onPlaceClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onLocate: () -> Unit,
    onRefresh: () -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_places_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (uiState.deviceRow != null) {
                        IconButton(onClick = onLocate, enabled = !uiState.isLocating) {
                            Icon(Icons.Filled.LocationOn, contentDescription = stringResource(R.string.action_update_location))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                LoadingView(modifier = Modifier.align(Alignment.Center))
            } else {
                PlacesList(uiState, onPlaceClick, onRemove, onLocate, onExplore)
            }
        }
    }
}

@Composable
private fun PlacesList(
    uiState: MyPlacesUiState,
    onPlaceClick: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onLocate: () -> Unit,
    onExplore: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        uiState.notice?.let { notice ->
            item(key = "notice") {
                FreshnessNote(text = notice.asString(), isStale = true, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            }
        }
        item(key = "device") {
            val deviceRow = uiState.deviceRow
            if (deviceRow == null) {
                LocationPrompt(isLocating = uiState.isLocating, onLocate = onLocate)
            } else {
                PlaceRow(row = deviceRow, onClick = { onPlaceClick(deviceRow.placeId) })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
        }
        if (uiState.rows.isEmpty()) {
            item(key = "empty") { EmptyPlaces(onExplore = onExplore) }
        } else {
            items(items = uiState.rows, key = { it.placeId }) { row ->
                Column(modifier = Modifier.animateItem()) {
                    RemovablePlaceRow(row = row, onClick = { onPlaceClick(row.placeId) }, onRemove = { onRemove(row.placeId) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

/**
 * Sola kaydırınca yer çıkarılır (snackbar'dan geri alınabilir). Kaydırma yapamayanlar için
 * aynı işlem TalkBack'in özel eylemler menüsünde de vardır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemovablePlaceRow(row: PlaceRowUi, onClick: () -> Unit, onRemove: () -> Unit) {
    val removeLabel = stringResource(R.string.action_remove_place)
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onRemove()
            value == SwipeToDismissBoxValue.EndToStart
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                EngravedLabel(text = removeLabel, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        modifier = Modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(removeLabel) {
                    onRemove()
                    true
                }
            )
        }
    ) {
        PlaceRow(row = row, onClick = onClick, modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}

@Composable
private fun LocationPrompt(isLocating: Boolean, onLocate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.location_prompt_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.location_prompt_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(onClick = onLocate, enabled = !isLocating, modifier = Modifier.padding(top = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Text(stringResource(if (isLocating) R.string.locating else R.string.action_use_location))
            }
        }
    }
}

@Composable
private fun EmptyPlaces(onExplore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.my_places_empty_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.my_places_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onExplore, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.action_go_explore)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPlacesScreenPreview() {
    UfukTheme {
        MyPlacesScreen(
            uiState = MyPlacesUiState(
                isLoading = false,
                rows = listOf("İstanbul" to 19f, "Trabzon" to 17f, "Antalya" to 27f).mapIndexed { index, (name, temperature) ->
                    PlaceRowUi(
                        placeId = index.toLong(),
                        title = UiText.Dynamic(name),
                        region = null,
                        detail = UiText.Dynamic("Parçalı bulutlu · 14:15"),
                        temperatureText = "${temperature.toInt()}°",
                        temperature = temperature,
                        todayMin = temperature - 6,
                        todayMax = temperature + 2,
                        rangeText = UiText.Dynamic("${temperature.toInt() + 2}° / ${temperature.toInt() - 6}°"),
                        glyph = GlyphKind.PartlyCloudy,
                        isDay = true,
                        isDeviceLocation = false,
                        isSaved = true,
                        description = UiText.Dynamic(name)
                    )
                }
            ),
            onPlaceClick = {},
            onRemove = {},
            onLocate = {},
            onRefresh = {},
            onExplore = {}
        )
    }
}
