package com.kampplus.ufuk.feature.forecast.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.component.ErrorView
import com.kampplus.ufuk.core.ui.component.FreshnessNote
import com.kampplus.ufuk.core.ui.component.GlyphKind
import com.kampplus.ufuk.core.ui.component.LoadingView
import com.kampplus.ufuk.core.ui.component.SaveToggleButton
import com.kampplus.ufuk.core.ui.component.degreesText
import com.kampplus.ufuk.core.ui.state.UiState
import com.kampplus.ufuk.core.ui.text.UiText
import com.kampplus.ufuk.core.ui.theme.UfukTheme
import com.kampplus.ufuk.feature.forecast.presentation.detail.component.DailyRangeList
import com.kampplus.ufuk.feature.forecast.presentation.detail.component.DialLegend
import com.kampplus.ufuk.feature.forecast.presentation.detail.component.HourlyChart
import com.kampplus.ufuk.feature.forecast.presentation.detail.component.InstrumentPanel
import com.kampplus.ufuk.feature.forecast.presentation.detail.component.TemperatureDial
import com.kampplus.ufuk.feature.forecast.presentation.model.DailyRangeUi
import com.kampplus.ufuk.feature.forecast.presentation.model.DayUi
import com.kampplus.ufuk.feature.forecast.presentation.model.ForecastUiModel
import com.kampplus.ufuk.feature.forecast.presentation.model.GaugeUi
import com.kampplus.ufuk.feature.forecast.presentation.model.HourlyUi
import com.kampplus.ufuk.feature.forecast.presentation.model.InstrumentsUi
import com.kampplus.ufuk.feature.forecast.presentation.model.ReadingUi
import com.kampplus.ufuk.feature.forecast.presentation.model.SunUi
import com.kampplus.ufuk.feature.forecast.presentation.model.TemperatureDialUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastDetailScreen(
    uiState: ForecastDetailUiState,
    onBack: () -> Unit,
    onShare: (ForecastUiModel) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forecast = (uiState.content as? UiState.Success)?.data
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.title.asString(),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitle = forecast?.subtitle
                        if (!subtitle.isNullOrEmpty()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    uiState.isSaved?.let { SaveToggleButton(isSaved = it, onClick = onToggleSaved) }
                    if (forecast != null) {
                        IconButton(onClick = { onShare(forecast) }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
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
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val content = uiState.content) {
                UiState.Loading -> LoadingView()
                UiState.Empty -> ErrorView(message = stringResource(R.string.error_not_found), onRetry = onRetry)
                is UiState.Error -> ErrorView(message = content.message.asString(), onRetry = onRetry)
                is UiState.Success -> ForecastContent(forecast = content.data, freshness = uiState.freshness)
            }
        }
    }
}

@Composable
private fun ForecastContent(forecast: ForecastUiModel, freshness: FreshnessUi?, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "dial") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TemperatureDial(
                    dial = forecast.dial,
                    condition = forecast.condition.asString(),
                    glyph = forecast.glyph,
                    isDay = forecast.isDay,
                    isStale = freshness?.isStale == true
                )
                DialLegend(yesterdayLegend = forecast.dial.yesterdayLegend?.asString())
                forecast.highLow?.let {
                    Text(
                        text = it.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item(key = "summary") {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                freshness?.let { FreshnessNote(text = it.text.asString(), isStale = it.isStale) }
                forecast.insights.forEachIndexed { index, insight ->
                    Text(
                        text = insight.asString(),
                        style = if (index == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        color = if (index == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (forecast.readings.isNotEmpty()) {
            item(key = "readings") { Readings(readings = forecast.readings, modifier = Modifier.padding(top = 24.dp)) }
        }
        item(key = "hourly") {
            Section(title = stringResource(R.string.section_hourly)) { HourlyChart(hours = forecast.hourly) }
        }
        item(key = "daily") {
            Section(title = stringResource(R.string.section_daily)) { DailyRangeList(daily = forecast.daily) }
        }
        item(key = "instruments") {
            Section(title = stringResource(R.string.section_instruments)) { InstrumentPanel(instruments = forecast.instruments) }
        }
        item(key = "attribution") {
            Text(
                text = stringResource(R.string.attribution),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

/** Üç okuma, aralarında kazıma çizgi: kutu yok, hiyerarşiyi yazı boyutu kurar. */
@Composable
private fun Readings(readings: List<ReadingUi>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        readings.forEachIndexed { index, reading ->
            if (index > 0) VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxHeight())
            val label = reading.label.asString()
            val value = reading.value.asString()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = "$label: $value" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                EngravedLabel(text = label)
                Text(text = degreesText(value), style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 36.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EngravedLabel(
            text = title,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .semantics { heading() }
        )
        content()
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun ForecastDetailScreenPreview() {
    UfukTheme {
        ForecastDetailScreen(
            uiState = ForecastDetailUiState(
                title = UiText.Dynamic("Ankara"),
                content = UiState.Success(previewForecast()),
                freshness = FreshnessUi(UiText.Dynamic("3 dk önce güncellendi"), isStale = false),
                isSaved = true
            ),
            onBack = {},
            onShare = {},
            onRefresh = {},
            onRetry = {},
            onToggleSaved = {}
        )
    }
}

private fun previewForecast() = ForecastUiModel(
    title = UiText.Dynamic("Ankara"),
    subtitle = "Ankara, Türkiye",
    dial = TemperatureDialUi(
        now = 22.4f,
        nowText = "22°",
        scaleMin = 5f,
        scaleMax = 30f,
        majorStep = 5,
        todayMin = 12f,
        todayMax = 23.5f,
        yesterday = 18.2f,
        yesterdayLegend = UiText.Dynamic("Dün bu saatte 18°"),
        description = UiText.Dynamic("")
    ),
    condition = UiText.Dynamic("Kapalı"),
    glyph = GlyphKind.Cloudy,
    isDay = true,
    highLow = UiText.Dynamic("En yüksek 24° · En düşük 12°"),
    insights = listOf(UiText.Dynamic("Saat 15:00 civarı yağmur başlıyor (olasılık %60)."), UiText.Dynamic("Dün bu saatten 4° daha sıcak.")),
    readings = listOf(
        ReadingUi(UiText.Dynamic("Hissedilen"), UiText.Dynamic("21°")),
        ReadingUi(UiText.Dynamic("Yağış olasılığı"), UiText.Dynamic("%70")),
        ReadingUi(UiText.Dynamic("Rüzgâr"), UiText.Dynamic("12 km/sa"))
    ),
    hourly = List(12) {
        HourlyUi(
            UiText.Dynamic(
                if (it ==
                    0
                ) {
                    "Şimdi"
                } else {
                    "${14 + it}:00"
                }
            ),
            it == 0,
            22f - it * 0.6f,
            "${22 - it / 2}°",
            GlyphKind.Rain,
            true,
            it * 7
        )
    },
    daily = DailyRangeUi(
        days = List(5) {
            DayUi(
                UiText.Dynamic(
                    "Pzt ${28 + it}"
                ),
                it == 0, GlyphKind.PartlyCloudy, 10f + it, 20f + it, "${10 + it}°", "${20 + it}°", null, UiText.Dynamic("")
            )
        },
        scaleMin = 10f,
        scaleMax = 24f,
        now = 16f
    ),
    instruments = InstrumentsUi(
        wind = GaugeUi(
            UiText.Dynamic("Rüzgâr"),
            UiText.Dynamic("12 km/sa"),
            UiText.Dynamic("Güneybatıdan · hamle 28 km/sa"),
            12f,
            0f,
            80f,
            direction = 45f
        ),
        humidity = GaugeUi(UiText.Dynamic("Nem"), UiText.Dynamic("%48"), UiText.Dynamic("Çiy noktası 11°"), 48f, 0f, 100f),
        pressure = GaugeUi(
            UiText.Dynamic("Basınç"),
            UiText.Dynamic("1010 hPa"),
            UiText.Dynamic("Son 3 saatte düşüyor"),
            1009.8f,
            960f,
            1060f,
            previous = 1011.6f
        ),
        uv = GaugeUi(UiText.Dynamic("UV indeksi"), UiText.Dynamic("3"), UiText.Dynamic("Orta · bugün en çok 4"), 3.4f, 0f, 12f),
        sun = SunUi(UiText.Dynamic("Güneş"), UiText.Dynamic("Batış 18:53"), UiText.Dynamic("Gün ışığı 11 sa 51 dk"), 0.61f),
        airQuality = GaugeUi(UiText.Dynamic("Hava kalitesi"), UiText.Dynamic("38"), UiText.Dynamic("Makul · PM2,5 11 µg/m³"), 38f, 0f, 100f)
    ),
    shareText = UiText.Dynamic("")
)
