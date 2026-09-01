package dev.boardmydelulu.anupam.data.repository

import dev.boardmydelulu.anupam.data.model.Sound
import dev.boardmydelulu.anupam.data.model.SoundDetail
import dev.boardmydelulu.anupam.data.network.NetworkModule
import dev.boardmydelulu.anupam.data.scraper.LocalScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SoundRepository {

    private val primary = NetworkModule.primaryApi
    private val fallback = NetworkModule.fallbackApi

    private fun resolveRegion(region: String): String =
        if (region.isBlank() || region.equals("global", ignoreCase = true) || region.equals("all", ignoreCase = true)) "us" else region.lowercase()

    suspend fun getTrending(region: String = "in", page: Int = 1): Result<List<Sound>> {
        val r = resolveRegion(region)
        return tryChain(
            api = { primary.getTrending(r, page).data ?: emptyList() },
            fallbackApi = { fallback.getTrending(r, page).data ?: emptyList() },
            scraper = { LocalScraper.getTrending(r, page) }
        )
    }

    suspend fun search(query: String, page: Int = 1): Result<List<Sound>> = tryChain(
        api = { primary.search(query, page).data ?: emptyList() },
        fallbackApi = { fallback.search(query, page).data ?: emptyList() },
        scraper = { LocalScraper.search(query, page) }
    )

    suspend fun getDetail(id: String): Result<SoundDetail> = tryChain(
        api = { primary.getDetail(id).data ?: throw Exception("Not found") },
        fallbackApi = { fallback.getDetail(id).data ?: throw Exception("Not found") },
        scraper = { LocalScraper.getDetail(id) }
    )

    suspend fun getRecent(page: Int = 1): Result<List<Sound>> = tryChain(
        api = { primary.getRecent(page).data ?: emptyList() },
        fallbackApi = { fallback.getRecent(page).data ?: emptyList() },
        scraper = { LocalScraper.getRecent(page) }
    )

    suspend fun getBest(region: String = "in", page: Int = 1): Result<List<Sound>> {
        val r = resolveRegion(region)
        return tryChain(
            api = { primary.getBest(r, page).data ?: emptyList() },
            fallbackApi = { fallback.getBest(r, page).data ?: emptyList() },
            scraper = { LocalScraper.getBest(r, page) }
        )
    }

    suspend fun getUploaded(username: String, page: Int = 1): Result<List<Sound>> = tryChain(
        api = { primary.getUploaded(username, page).data ?: emptyList() },
        fallbackApi = { fallback.getUploaded(username, page).data ?: emptyList() },
        scraper = { LocalScraper.getUploaded(username, page) }
    )

    suspend fun getUserFavorites(username: String, page: Int = 1): Result<List<Sound>> = tryChain(
        api = { primary.getFavorites(username, page).data ?: emptyList() },
        fallbackApi = { fallback.getFavorites(username, page).data ?: emptyList() },
        scraper = { LocalScraper.getUserFavorites(username, page) }
    )

    private suspend fun <T> tryChain(
        api: suspend () -> T,
        fallbackApi: suspend () -> T,
        scraper: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(api())
        } catch (_: Exception) {
            try {
                Result.success(fallbackApi())
            } catch (_: Exception) {
                try {
                    Result.success(scraper())
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }
}
