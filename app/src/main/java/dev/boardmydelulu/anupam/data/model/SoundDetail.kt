package dev.boardmydelulu.anupam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SoundDetail(
    val id: String,
    val url: String,
    val title: String,
    val mp3: String,
    val description: String,
    val tags: List<String>,
    val favorites: String,
    val views: String,
    val uploader: Uploader
)

@Serializable
data class Uploader(
    val username: String,
    val url: String
)
