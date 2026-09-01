package dev.boardmydelulu.anupam.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.boardmydelulu.anupam.data.BoardDatabase
import dev.boardmydelulu.anupam.data.model.Sound
import dev.boardmydelulu.anupam.data.model.SoundDetail
import dev.boardmydelulu.anupam.data.repository.FavoriteRepository
import dev.boardmydelulu.anupam.data.repository.SoundRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class HomeCategory(val label: String) {
    TRENDING("Trending"),
    RECENT("Recent"),
    BEST("Best of All Time")
}

@Stable
data class Region(val code: String, val name: String, val flag: String)

val regions = listOf(
    Region("global", "Global / All", "🌐"),
    Region("in", "India", "🇮🇳"),
    Region("us", "United States", "🇺🇸"),
    Region("gb", "United Kingdom", "🇬🇧"),
    Region("br", "Brazil", "🇧🇷"),
    Region("de", "Germany", "🇩🇪"),
    Region("fr", "France", "🇫🇷"),
    Region("id", "Indonesia", "🇮🇩"),
    Region("mx", "Mexico", "🇲🇽"),
    Region("es", "Spain", "🇪🇸"),
    Region("it", "Italy", "🇮🇹"),
    Region("jp", "Japan", "🇯🇵"),
    Region("kr", "South Korea", "🇰🇷"),
    Region("au", "Australia", "🇦🇺"),
    Region("ca", "Canada", "🇨🇦"),
    Region("ru", "Russia", "🇷🇺"),
    Region("tr", "Turkey", "🇹🇷"),
    Region("ph", "Philippines", "🇵🇭"),
    Region("th", "Thailand", "🇹🇭"),
    Region("vn", "Vietnam", "🇻🇳"),
    Region("pk", "Pakistan", "🇵🇰"),
    Region("bd", "Bangladesh", "🇧🇩"),
    Region("sa", "Saudi Arabia", "🇸🇦"),
    Region("ae", "United Arab Emirates", "🇦🇪"),
    Region("ar", "Argentina", "🇦🇷"),
    Region("co", "Colombia", "🇨🇴"),
    Region("cl", "Chile", "🇨🇱"),
    Region("pl", "Poland", "🇵🇱"),
    Region("nl", "Netherlands", "🇳🇱"),
    Region("se", "Sweden", "🇸🇪"),
    Region("pt", "Portugal", "🇵🇹"),
    Region("eg", "Egypt", "🇪🇬"),
    Region("za", "South Africa", "🇿🇦"),
    Region("ng", "Nigeria", "🇳🇬"),
    Region("my", "Malaysia", "🇲🇾"),
    Region("sg", "Singapore", "🇸🇬"),
    Region("nz", "New Zealand", "🇳🇿")
)

data class BoardUiState(
    val isSplashLoading: Boolean = true,
    val splashError: String? = null,
    val homeCategory: HomeCategory = HomeCategory.TRENDING,
    val selectedRegion: Region = regions[0],
    val homeSounds: List<Sound> = emptyList(),
    val isHomeLoading: Boolean = false,
    val isLoadingMoreHome: Boolean = false,
    val homePage: Int = 1,
    val trending: List<Sound> = emptyList(),
    val recent: List<Sound> = emptyList(),
    val best: List<Sound> = emptyList(),
    val favorites: List<Sound> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val searchResults: List<Sound> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val searchPage: Int = 1,
    val detail: SoundDetail? = null,
    val isDetailLoading: Boolean = false
)

class BoardViewModel(application: Application) : AndroidViewModel(application) {
    private val soundRepo = SoundRepository()
    private val favoriteRepo = FavoriteRepository(BoardDatabase.getInstance(application))
    private val prefs = application.getSharedPreferences("boardmydelulu_prefs", Context.MODE_PRIVATE)
    private var searchJob: Job? = null
    private var autoRefreshJob: Job? = null

    private val _uiState = MutableStateFlow(BoardUiState())
    val uiState: StateFlow<BoardUiState> = _uiState

    init {
        val savedRegionCode = prefs.getString("region_code", "global") ?: "global"
        val savedRegion = regions.find { it.code == savedRegionCode } ?: regions[0]
        _uiState.value = _uiState.value.copy(selectedRegion = savedRegion)

        loadInitialData()
        viewModelScope.launch {
            favoriteRepo.allFavorites.collectLatest { favs ->
                _uiState.value = _uiState.value.copy(favorites = favs)
            }
        }
        viewModelScope.launch {
            favoriteRepo.favoriteIds.collectLatest { ids ->
                _uiState.value = _uiState.value.copy(favoriteIds = ids.toSet())
            }
        }
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(45_000)
                val current = _uiState.value
                if (current.homeSounds.isNotEmpty()) {
                    _uiState.value = current.copy(homeSounds = current.homeSounds.shuffled())
                }
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSplashLoading = true, splashError = null, homePage = 1)
            val region = _uiState.value.selectedRegion.code
            val trendingResult = soundRepo.getTrending(region, 1)
            val recentResult = soundRepo.getRecent(1)
            val trending = trendingResult.getOrDefault(emptyList())
            val recent = recentResult.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                trending = trending,
                recent = recent,
                homeSounds = trending,
                homeCategory = HomeCategory.TRENDING,
                homePage = 1,
                isSplashLoading = false,
                splashError = if (trendingResult.isFailure && recentResult.isFailure) "No internet connection" else null
            )
        }
    }

    fun changeRegion(region: Region) {
        if (_uiState.value.selectedRegion.code == region.code) return
        prefs.edit().putString("region_code", region.code).apply()
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            trending = emptyList(),
            best = emptyList(),
            homeSounds = emptyList(),
            homePage = 1
        )
        switchCategory(_uiState.value.homeCategory)
    }

    fun switchCategory(category: HomeCategory) {
        val cached = when (category) {
            HomeCategory.TRENDING -> _uiState.value.trending
            HomeCategory.RECENT -> _uiState.value.recent
            HomeCategory.BEST -> _uiState.value.best
        }
        if (cached.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                homeCategory = category,
                homeSounds = cached,
                isHomeLoading = false,
                homePage = 1
            )
            return
        }
        _uiState.value = _uiState.value.copy(
            homeCategory = category,
            homeSounds = emptyList(),
            isHomeLoading = true,
            homePage = 1
        )
        viewModelScope.launch {
            val region = _uiState.value.selectedRegion.code
            val result = when (category) {
                HomeCategory.TRENDING -> soundRepo.getTrending(region, 1)
                HomeCategory.RECENT -> soundRepo.getRecent(1)
                HomeCategory.BEST -> soundRepo.getBest(region, 1)
            }
            val sounds = result.getOrDefault(emptyList())
            val updated = when (category) {
                HomeCategory.TRENDING -> _uiState.value.copy(trending = sounds)
                HomeCategory.RECENT -> _uiState.value.copy(recent = sounds)
                HomeCategory.BEST -> _uiState.value.copy(best = sounds)
            }
            _uiState.value = updated.copy(homeSounds = sounds, isHomeLoading = false, homePage = 1)
        }
    }

    fun loadNextHomePage() {
        val current = _uiState.value
        if (current.isHomeLoading || current.isLoadingMoreHome) return
        val nextPage = current.homePage + 1
        val region = current.selectedRegion.code
        val cat = current.homeCategory

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMoreHome = true)
            val result = when (cat) {
                HomeCategory.TRENDING -> soundRepo.getTrending(region, nextPage)
                HomeCategory.RECENT -> soundRepo.getRecent(nextPage)
                HomeCategory.BEST -> soundRepo.getBest(region, nextPage)
            }
            val newSounds = result.getOrDefault(emptyList())
            if (newSounds.isNotEmpty()) {
                val combined = current.homeSounds + newSounds
                val updated = when (cat) {
                    HomeCategory.TRENDING -> _uiState.value.copy(trending = combined)
                    HomeCategory.RECENT -> _uiState.value.copy(recent = combined)
                    HomeCategory.BEST -> _uiState.value.copy(best = combined)
                }
                _uiState.value = updated.copy(
                    homeSounds = combined,
                    homePage = nextPage,
                    isLoadingMoreHome = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMoreHome = false)
            }
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, searchPage = 1)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), hasSearched = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(isSearching = true)
            val result = soundRepo.search(query, 1)
            _uiState.value = _uiState.value.copy(
                searchResults = result.getOrDefault(emptyList()),
                isSearching = false,
                hasSearched = true,
                searchPage = 1
            )
        }
    }

    fun loadNextSearchPage() {
        val current = _uiState.value
        if (current.searchQuery.isBlank() || current.isSearching) return
        val nextPage = current.searchPage + 1
        viewModelScope.launch {
            _uiState.value = current.copy(isSearching = true)
            val result = soundRepo.search(current.searchQuery, nextPage)
            val newSounds = result.getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                searchResults = current.searchResults + newSounds,
                isSearching = false,
                searchPage = nextPage
            )
        }
    }

    fun loadDetail(soundId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDetailLoading = true, detail = null)
            val result = soundRepo.getDetail(soundId)
            _uiState.value = _uiState.value.copy(
                detail = result.getOrNull(),
                isDetailLoading = false
            )
        }
    }

    fun toggleFavorite(sound: Sound) {
        viewModelScope.launch {
            val isFav = _uiState.value.favoriteIds.contains(sound.id)
            favoriteRepo.toggleFavorite(sound, isFav)
        }
    }
}
