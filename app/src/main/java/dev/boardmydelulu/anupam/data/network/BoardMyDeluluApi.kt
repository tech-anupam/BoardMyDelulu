package dev.boardmydelulu.anupam.data.network

import dev.boardmydelulu.anupam.data.model.ApiResponse
import dev.boardmydelulu.anupam.data.model.Sound
import dev.boardmydelulu.anupam.data.model.SoundDetail
import retrofit2.http.GET
import retrofit2.http.Query

interface BoardMyDeluluApi {

    @GET("trending")
    suspend fun getTrending(
        @Query("q") region: String,
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>

    @GET("detail")
    suspend fun getDetail(@Query("id") id: String): ApiResponse<SoundDetail>

    @GET("recent")
    suspend fun getRecent(
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>

    @GET("best")
    suspend fun getBest(
        @Query("q") region: String,
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>

    @GET("uploaded")
    suspend fun getUploaded(
        @Query("username") username: String,
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>

    @GET("favorites")
    suspend fun getFavorites(
        @Query("username") username: String,
        @Query("page") page: Int = 1
    ): ApiResponse<List<Sound>>
}
