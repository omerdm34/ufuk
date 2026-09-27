package com.kampplus.ufuk.feature.forecast.presentation.myplaces

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kampplus.ufuk.R
import com.kampplus.ufuk.core.model.Place

/**
 * Konum izni burada, Route katmanında istenir: izin penceresi platformun işidir, ViewModel yalnızca
 * sonucu ("izin verildi, konumu bul" ya da "reddedildi") öğrenir. Kaba konum yeterlidir.
 */
@Composable
fun MyPlacesRoute(
    onPlaceClick: (Place) -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPlacesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) viewModel.onLocate() else viewModel.onLocationPermissionDenied()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPlacesEvent.PlaceRemoved -> {
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(R.string.place_removed, event.title.asString(resources)),
                        actionLabel = resources.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoRemove(event.removed)
                }
                is MyPlacesEvent.Message -> snackbarHostState.showSnackbar(event.text.asString(resources))
            }
        }
    }

    MyPlacesScreen(
        uiState = uiState,
        onPlaceClick = { id -> viewModel.findPlace(id)?.let(onPlaceClick) },
        onRemove = viewModel::onRemove,
        onLocate = { permissionLauncher.launch(LOCATION_PERMISSIONS) },
        onRefresh = viewModel::onRefresh,
        onExplore = onExplore,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

private val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
