package com.kampplus.ufuk.feature.settings.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.ufuk.BuildConfig

@Composable
fun SettingsRoute(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val units by viewModel.units.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsScreen(
        units = units,
        versionName = BuildConfig.VERSION_NAME,
        onTemperatureUnitSelected = viewModel::onTemperatureUnitSelected,
        onWindSpeedUnitSelected = viewModel::onWindSpeedUnitSelected,
        onOpenLink = { url -> context.openLink(url) },
        modifier = modifier
    )
}

/** Tarayıcı yoksa (ör. kısıtlı iş profili) sessizce vazgeçilir; ayarlar ekranı çökmez. */
private fun Context.openLink(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        // Açacak uygulama yok.
    }
}
