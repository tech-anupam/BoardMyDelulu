package dev.boardmydelulu.anupam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    val id: String,
    val title: String,
    val url: String,
    val mp3: String
)
