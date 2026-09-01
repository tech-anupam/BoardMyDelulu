package dev.boardmydelulu.anupam.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.boardmydelulu.anupam.ui.BoardViewModel
import dev.boardmydelulu.anupam.ui.screen.DownloadsScreen
import dev.boardmydelulu.anupam.ui.screen.FavoritesScreen
import dev.boardmydelulu.anupam.ui.screen.HomeScreen
import dev.boardmydelulu.anupam.ui.screen.PlayerScreen
import dev.boardmydelulu.anupam.ui.screen.SearchScreen
import dev.boardmydelulu.anupam.ui.screen.SettingsScreen

@Composable
fun BoardApp(viewModel: BoardViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomNavItem.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 2.dp) {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (currentRoute == item.route) item.selectedIcon else item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Routes.HOME, modifier = Modifier.padding(paddingValues)) {
            composable(Routes.HOME) {
                HomeScreen(
                    onSoundClick = { id -> navController.navigate(Routes.playerRoute(id)) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    viewModel = viewModel
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onSoundClick = { id -> navController.navigate(Routes.playerRoute(id)) },
                    viewModel = viewModel
                )
            }
            composable(Routes.DOWNLOADS) { DownloadsScreen() }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onSoundClick = { id -> navController.navigate(Routes.playerRoute(id)) },
                    viewModel = viewModel
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel = viewModel) }
            composable(
                route = Routes.PLAYER,
                arguments = listOf(navArgument("soundId") { type = NavType.StringType })
            ) { backStackEntry ->
                val soundId = backStackEntry.arguments?.getString("soundId") ?: ""
                PlayerScreen(soundId = soundId, onNavigateBack = { navController.popBackStack() }, viewModel = viewModel)
            }
        }
    }
}
