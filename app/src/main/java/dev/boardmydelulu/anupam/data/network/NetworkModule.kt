package dev.boardmydelulu.anupam.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val PRIMARY_URL = "https://boardmydelulu-api.vercel.app/"
    private const val FALLBACK_URL = "https://myinstants-api.vercel.app/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    private fun buildApi(baseUrl: String): BoardMyDeluluApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BoardMyDeluluApi::class.java)
    }

    val primaryApi: BoardMyDeluluApi by lazy { buildApi(PRIMARY_URL) }
    val fallbackApi: BoardMyDeluluApi by lazy { buildApi(FALLBACK_URL) }
}
