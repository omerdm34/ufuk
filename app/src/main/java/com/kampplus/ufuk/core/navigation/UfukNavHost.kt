package com.kampplus.ufuk.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kampplus.ufuk.core.model.Place
import com.kampplus.ufuk.feature.forecast.presentation.detail.ForecastDetailRoute
import com.kampplus.ufuk.feature.forecast.presentation.explore.ExploreRoute
import com.kampplus.ufuk.feature.forecast.presentation.myplaces.MyPlacesRoute
import com.kampplus.ufuk.feature.settings.presentation.SettingsRoute

/** Composition root: ekranlar birbirini tanımaz, geçişler yalnızca burada tanımlanır. */
@Composable
fun UfukNavHost(navController: NavHostController, onNavigateTopLevel: (TopLevelDestination) -> Unit, modifier: Modifier = Modifier) {
    val openForecast: (Place) -> Unit = { place -> navController.navigate(place.toForecastDestination()) }
    NavHost(
        navController = navController,
        startDestination = MyPlacesDestination,
        modifier = modifier
    ) {
        composable<MyPlacesDestination> {
            MyPlacesRoute(onPlaceClick = openForecast, onExplore = { onNavigateTopLevel(TopLevelDestination.Explore) })
        }
        composable<ExploreDestination> {
            ExploreRoute(onPlaceClick = openForecast)
        }
        composable<SettingsDestination> {
            SettingsRoute()
        }
        composable<ForecastDestination> {
            ForecastDetailRoute(onBack = navController::navigateUp)
        }
    }
}
