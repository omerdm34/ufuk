package com.kampplus.ufuk.feature.forecast.presentation.explore

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.EmptyView
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.component.ErrorView
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.component.LoadingView
import com.kampplus.ufuk.core.ui.component.SaveToggleButton
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.theme.UfukTheme
import com.kampplus.ufuk.feature.forecast.presentation.component.PlaceRow
import com.kampplus.ufuk.feature.forecast.presentation.model.PlaceRowUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    onQueryChange: (String) -> Unit,
    onSortChange: (ExploreSort) -> Unit,
    onPlaceClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.explore_title), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            PullToRefreshBox(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when (val content = uiState.content) {
                        UiState.Loading -> LoadingView()
                        UiState.Empty -> EmptyView(
                            title = stringResource(R.string.search_empty_title),
                            message = stringResource(R.string.search_empty_message, uiState.query.trim()),
                            glyph = GlyphKind.Fog
                        )
                        is UiState.Error -> ErrorView(message = content.message.asString(), onRetry = onRetry)
                        is UiState.Success -> ExploreList(uiState, content.data, onSortChange, onPlaceClick, onToggleSaved)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreList(
    uiState: ExploreUiState,
    rows: List<PlaceRowUi>,
    onSortChange: (ExploreSort) -> Unit,
    onPlaceClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        if (!uiState.isSearching) {
            item(key = "header") {
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EngravedLabel(text = stringResource(R.string.explore_now_title), modifier = Modifier.semantics { heading() })
                    uiState.extremes?.let { Text(text = it.asString(), style = MaterialTheme.typography.bodyLarge) }
                    SortChips(selected = uiState.sort, onSortChange = onSortChange)
                }
            }
        }
        items(items = rows, key = { it.placeId }) { row ->
            Column(modifier = Modifier.animateItem()) {
                PlaceRow(
                    row = row,
                    onClick = { onPlaceClick(row.placeId) },
                    trailing = { SaveToggleButton(isSaved = row.isSaved, onClick = { onToggleSaved(row.placeId) }) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun SortChips(selected: ExploreSort, onSortChange: (ExploreSort) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExploreSort.entries.forEach { sort ->
            FilterChip(
                selected = sort == selected,
                onClick = { onSortChange(sort) },
                label = {
                    Text(
                        stringResource(
                            when (sort) {
                                ExploreSort.Alphabetical -> R.string.sort_alphabetical
                                ExploreSort.Warmest -> R.string.sort_warmest
                                ExploreSort.Coldest -> R.string.sort_coldest
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenPreview() {
    UfukTheme {
        ExploreScreen(
            uiState = ExploreUiState(
                content = UiState.Success(
                    listOf("Adana" to 31f, "Ankara" to 22f, "Erzurum" to 11f).mapIndexed { index, (name, temperature) ->
                        PlaceRowUi(
                            placeId = index.toLong(),
                            title = UiText.Dynamic(name),
                            region = null,
                            detail = UiText.Dynamic("Açık · 14:15"),
                            temperatureText = "${temperature.toInt()}°",
                            temperature = temperature,
                            todayMin = temperature - 8,
                            todayMax = temperature + 1,
                            rangeText = UiText.Dynamic("${temperature.toInt() + 1}° / ${temperature.toInt() - 8}°"),
                            glyph = GlyphKind.Clear,
                            isDay = true,
                            isDeviceLocation = false,
                            isSaved = index == 1,
                            description = UiText.Dynamic(name)
                        )
                    }
                ),
                extremes = UiText.Dynamic("En sıcak Adana 31° · En serin Erzurum 11°")
            ),
            onQueryChange = {},
            onSortChange = {},
            onPlaceClick = {},
            onToggleSaved = {},
            onRetry = {},
            onRefresh = {}
        )
    }
}
