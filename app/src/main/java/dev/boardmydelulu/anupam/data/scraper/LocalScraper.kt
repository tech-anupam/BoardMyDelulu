package dev.boardmydelulu.anupam.data.scraper

import dev.boardmydelulu.anupam.data.model.Sound
import dev.boardmydelulu.anupam.data.model.SoundDetail
import dev.boardmydelulu.anupam.data.model.Uploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object LocalScraper {

    private const val BASE = "https://www.myinstants.com"
    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

    private suspend fun fetchAndParse(url: String): List<Sound> = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).userAgent(UA).followRedirects(true).get()
        val sounds = mutableListOf<Sound>()
        doc.select("div.instant").forEach { el ->
            val link = el.selectFirst("a.instant-link") ?: return@forEach
            val title = link.text().trim()
            val href = link.attr("href")
            val soundUrl = BASE + href
            val id = href.removePrefix("/en/instant/").trimEnd('/')
            val btn = el.selectFirst("button.small-button")
            val onclick = btn?.attr("onclick") ?: ""
            val match = Regex("play\\('(.*?)'").find(onclick)
            if (match != null) {
                sounds.add(Sound(id = id, title = title, url = soundUrl, mp3 = BASE + match.groupValues[1]))
            }
        }
        sounds
    }

    private fun resolveRegion(region: String): String =
        if (region.isBlank() || region.equals("global", ignoreCase = true) || region.equals("all", ignoreCase = true)) "us" else region.lowercase()

    suspend fun getTrending(region: String, page: Int = 1): List<Sound> {
        val r = resolveRegion(region)
        val pageParam = if (page > 1) "?page=$page" else ""
        return fetchAndParse("$BASE/en/index/$r/$pageParam")
    }

    suspend fun search(query: String, page: Int = 1): List<Sound> {
        val pageParam = if (page > 1) "&page=$page" else ""
        return fetchAndParse("$BASE/en/search/?name=${java.net.URLEncoder.encode(query, "UTF-8")}$pageParam")
    }

    suspend fun getRecent(page: Int = 1): List<Sound> {
        val pageParam = if (page > 1) "?page=$page" else ""
        return fetchAndParse("$BASE/en/recent/$pageParam")
    }

    suspend fun getBest(region: String, page: Int = 1): List<Sound> {
        val r = resolveRegion(region)
        val pageParam = if (page > 1) "?page=$page" else ""
        return fetchAndParse("$BASE/en/best_of_all_time/$r/$pageParam")
    }

    suspend fun getUploaded(username: String, page: Int = 1): List<Sound> {
        val pageParam = if (page > 1) "?page=$page" else ""
        return fetchAndParse("$BASE/en/profile/${java.net.URLEncoder.encode(username, "UTF-8")}/uploaded/$pageParam")
    }

    suspend fun getUserFavorites(username: String, page: Int = 1): List<Sound> {
        val pageParam = if (page > 1) "?page=$page" else ""
        return fetchAndParse("$BASE/en/profile/${java.net.URLEncoder.encode(username, "UTF-8")}/$pageParam")
    }

    suspend fun getDetail(id: String): SoundDetail = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect("$BASE/en/instant/$id").userAgent(UA).followRedirects(true).get()
        val title = doc.selectFirst("h1#instant-page-title")?.text()?.trim() ?: ""
        val soundUrl = doc.selectFirst("button#instant-page-button-element")?.attr("data-url") ?: ""
        val description = doc.selectFirst("div#instant-page-description p")?.text()?.trim() ?: ""
        val tags = doc.select("div#instant-page-tags a").map { it.text().trim() }
        val favText = doc.selectFirst("div#instant-page-likes b")?.text()?.trim() ?: "0"
        val favorites = favText.replace(" users", "")
        val authorDiv = doc.select("div#instant-page-likes ~ div").getOrNull(1)
        val uploaderLink = authorDiv?.selectFirst("a")
        val username = uploaderLink?.text()?.trim() ?: ""
        val uploaderUrl = BASE + (uploaderLink?.attr("href") ?: "")
        val authorText = authorDiv?.text()?.trim() ?: ""
        val views = authorText.replace("views", "").replace("Uploaded by $username - ", "").trim()
        SoundDetail(
            id = id,
            url = "$BASE/en/instant/$id",
            title = title,
            mp3 = BASE + soundUrl,
            description = description,
            tags = tags,
            favorites = favorites,
            views = views,
            uploader = Uploader(username = username, url = uploaderUrl)
        )
    }
}
