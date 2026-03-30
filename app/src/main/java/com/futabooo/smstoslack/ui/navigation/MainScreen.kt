package com.futabooo.smstoslack.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.ui.screen.dashboard.DashboardScreen
import com.futabooo.smstoslack.ui.screen.filter.FilterScreen
import com.futabooo.smstoslack.ui.screen.history.HistoryScreen
import com.futabooo.smstoslack.ui.screen.settings.SettingsScreen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Route.DASHBOARD, "ダッシュボード", Icons.Default.Home),
    BottomNavItem(Route.SETTINGS, "設定", Icons.Default.Settings),
    BottomNavItem(Route.FILTERS, "フィルタ", Icons.Default.FilterList),
    BottomNavItem(Route.HISTORY, "履歴", Icons.Default.History)
)

@Composable
fun MainScreen(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.DASHBOARD) {
                DashboardScreen(appContainer = appContainer)
            }
            composable(Route.SETTINGS) {
                SettingsScreen(appContainer = appContainer)
            }
            composable(Route.FILTERS) {
                FilterScreen(appContainer = appContainer)
            }
            composable(Route.HISTORY) {
                HistoryScreen(appContainer = appContainer)
            }
        }
    }
}
