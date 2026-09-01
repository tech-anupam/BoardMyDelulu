package dev.boardmydelulu.anupam.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(Routes.HOME, "Sounds", Icons.Outlined.Home, Icons.Filled.Home),
    SEARCH(Routes.SEARCH, "Search", Icons.Outlined.Search, Icons.Filled.Search),
    DOWNLOADS(Routes.DOWNLOADS, "Downloads", Icons.Outlined.Download, Icons.Filled.Download),
    FAVORITES(Routes.FAVORITES, "Favorites", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}
