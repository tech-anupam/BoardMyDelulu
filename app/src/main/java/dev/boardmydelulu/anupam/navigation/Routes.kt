package dev.boardmydelulu.anupam.navigation

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DOWNLOADS = "downloads"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{soundId}"

    fun playerRoute(soundId: String) = "player/$soundId"
}
