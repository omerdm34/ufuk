package com.kampplus.ufuk

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kampplus.ufuk.core.navigation.TopLevelDestination
import com.kampplus.ufuk.core.navigation.UfukNavHost
import com.kampplus.ufuk.core.navigation.UfukNavigationBar
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

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // Her ekranın kendi Scaffold'u üst çubuğu ve sistem çubuklarının boşluğunu yönetir.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (isTopLevel) UfukNavigationBar(currentDestination = currentDestination, onNavigate = navigateTopLevel)
        }
    ) { innerPadding ->
        UfukNavHost(
            navController = navController,
            onNavigateTopLevel = navigateTopLevel,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}
