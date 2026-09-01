package dev.boardmydelulu.anupam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val soundId: String,
    val title: String,
    val url: String,
    val mp3: String,
    val savedAt: Long = System.currentTimeMillis()
)
