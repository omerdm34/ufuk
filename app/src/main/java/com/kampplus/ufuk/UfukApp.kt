package com.kampplus.ufuk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kampplus.ufuk.core.navigation.TopLevelDestination
import com.kampplus.ufuk.core.navigation.UfukNavHost
import com.kampplus.ufuk.core.navigation.UfukNavigationBar
import com.kampplus.ufuk.core.navigation.UfukNavigationRail
import com.kampplus.ufuk.core.navigation.isOn

@Composable
fun UfukApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val isTopLevel = TopLevelDestination.entries.any { currentDestination.isOn(it) }
    val navigateTopLevel: (TopLevelDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val isExpanded = windowWidth >= EXPANDED_WIDTH

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // Her ekranın kendi Scaffold'u üst çubuğu ve sistem çubuklarının boşluğunu yönetir.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (isTopLevel && !isExpanded) UfukNavigationBar(currentDestination = currentDestination, onNavigate = navigateTopLevel)
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (isTopLevel && isExpanded) UfukNavigationRail(currentDestination = currentDestination, onNavigate = navigateTopLevel)
            // Geniş ekranda satırlar okunur uzunlukta kalsın diye içerik ortada bir sütunda durur.
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                UfukNavHost(
                    navController = navController,
                    onNavigateTopLevel = navigateTopLevel,
                    modifier = Modifier
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/** Material 3 pencere sınıfı: 600dp ve üstü "orta" genişlik, ray kullanılır. */
private val EXPANDED_WIDTH = 600.dp

private val MAX_CONTENT_WIDTH = 720.dp
