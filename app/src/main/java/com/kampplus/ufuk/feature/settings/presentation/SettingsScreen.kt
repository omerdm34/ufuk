package com.kampplus.ufuk.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.TemperatureUnit
import com.kampplus.ufuk.core.model.UnitSettings
import com.kampplus.ufuk.core.model.WindSpeedUnit
import com.kampplus.ufuk.core.ui.component.EngravedLabel
import com.kampplus.ufuk.core.ui.theme.UfukTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    units: UnitSettings,
    versionName: String,
    onTemperatureUnitSelected: (TemperatureUnit) -> Unit,
    onWindSpeedUnitSelected: (WindSpeedUnit) -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            SectionHeader(stringResource(R.string.settings_units))
            UnitChoice(
                label = stringResource(R.string.settings_temperature),
                options = TemperatureUnit.entries,
                selected = units.temperature,
                optionLabel = { stringResource(it.labelRes()) },
                onSelected = onTemperatureUnitSelected
            )
            UnitChoice(
                label = stringResource(R.string.settings_wind),
                options = WindSpeedUnit.entries,
                selected = units.windSpeed,
                optionLabel = { stringResource(it.labelRes()) },
                onSelected = onWindSpeedUnitSelected
            )

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SectionHeader(stringResource(R.string.settings_about))
            LinkRow(
                title = stringResource(R.string.settings_data_source),
                supporting = stringResource(R.string.settings_data_source_detail),
                onClick = { onOpenLink(OPEN_METEO_URL) }
            )
            LinkRow(
                title = stringResource(R.string.settings_font),
                supporting = stringResource(R.string.settings_font_detail),
                onClick = { onOpenLink(BARLOW_URL) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = { Text(stringResource(R.string.settings_version, versionName)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    EngravedLabel(text = text, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> UnitChoice(label: String, options: List<T>, selected: T, optionLabel: @Composable (T) -> String, onSelected: (T) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) {
                    Text(optionLabel(option), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun LinkRow(title: String, supporting: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(supporting) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick)
    )
}

private fun TemperatureUnit.labelRes() = when (this) {
    TemperatureUnit.Celsius -> R.string.unit_celsius
    TemperatureUnit.Fahrenheit -> R.string.unit_fahrenheit
}

private fun WindSpeedUnit.labelRes() = when (this) {
    WindSpeedUnit.KilometresPerHour -> R.string.unit_kmh
    WindSpeedUnit.MetresPerSecond -> R.string.unit_ms
    WindSpeedUnit.MilesPerHour -> R.string.unit_mph
}

private const val OPEN_METEO_URL = "https://open-meteo.com/"
private const val BARLOW_URL = "https://github.com/jpt/barlow"

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    UfukTheme {
        SettingsScreen(
            units = UnitSettings(),
            versionName = "1.0.0",
            onTemperatureUnitSelected = {},
            onWindSpeedUnitSelected = {},
            onOpenLink = {}
        )
    }
}
