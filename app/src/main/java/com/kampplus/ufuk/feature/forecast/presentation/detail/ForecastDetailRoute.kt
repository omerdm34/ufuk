package com.kampplus.ufuk.feature.forecast.presentation.detail

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.ufuk.R
import com.kampplus.ufuk.feature.forecast.presentation.model.ForecastUiModel

/**
 * ViewModel'i ekrana bağlayan katman. Kaydetme düğmesi yerler özelliğine aittir; bu ekran onu
 * yalnızca durum ([isSaved]) ve olay ([onToggleSaved]) olarak alır, özellikler birbirini tanımaz.
 */
@Composable
fun ForecastDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean? = null,
    onToggleSaved: () -> Unit = {},
    viewModel: ForecastDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ForecastDetailScreen(
        uiState = uiState.copy(isSaved = isSaved),
        onBack = onBack,
        onShare = { forecast -> context.shareForecast(forecast) },
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onToggleSaved = onToggleSaved,
        modifier = modifier
    )
}

/** Android paylaşım penceresini açar. Platform bağımlılığı Route katmanında kalır. */
private fun Context.shareForecast(forecast: ForecastUiModel) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, forecast.shareText.asString(resources))
    }
    startActivity(Intent.createChooser(sendIntent, getString(R.string.action_share)))
}
