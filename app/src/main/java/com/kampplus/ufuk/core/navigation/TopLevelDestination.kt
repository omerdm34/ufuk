package com.kampplus.ufuk.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.kampplus.ufuk.R
import kotlin.reflect.KClass

/** Alt gezinme çubuğundaki sekmeler. Yeni sekme = yeni enum değeri + NavHost'ta bir `composable`. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int
) {
    MyPlaces(MyPlacesDestination, MyPlacesDestination::class, Icons.Filled.Place, Icons.Outlined.Place, R.string.nav_my_places),
    Explore(ExploreDestination, ExploreDestination::class, Icons.Filled.Search, Icons.Outlined.Search, R.string.nav_explore),
    Settings(SettingsDestination, SettingsDestination::class, Icons.Filled.Settings, Icons.Outlined.Settings, R.string.nav_settings)
}
