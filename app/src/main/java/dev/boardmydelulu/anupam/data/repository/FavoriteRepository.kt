package dev.boardmydelulu.anupam.data.repository

import dev.boardmydelulu.anupam.data.BoardDatabase
import dev.boardmydelulu.anupam.data.FavoriteEntity
import dev.boardmydelulu.anupam.data.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val db: BoardDatabase) {

    val allFavorites: Flow<List<Sound>> =
        db.favoriteDao().getAll().map { entities ->
            entities.map { it.toSound() }
        }

    val favoriteIds: Flow<List<String>> = db.favoriteDao().getAllIds()

    val count: Flow<Int> = db.favoriteDao().count()

    fun isFavorite(soundId: String): Flow<Boolean> = db.favoriteDao().isFavorite(soundId)

    suspend fun addFavorite(sound: Sound) {
        db.favoriteDao().insert(
            FavoriteEntity(
                soundId = sound.id,
                title = sound.title,
                url = sound.url,
                mp3 = sound.mp3
            )
        )
    }

    suspend fun removeFavorite(soundId: String) {
        db.favoriteDao().deleteById(soundId)
    }

    suspend fun toggleFavorite(sound: Sound, isFavorite: Boolean) {
        if (isFavorite) removeFavorite(sound.id) else addFavorite(sound)
    }

    suspend fun clearAll() {
        db.favoriteDao().clearAll()
    }

    private fun FavoriteEntity.toSound() = Sound(
        id = soundId,
        title = title,
        url = url,
        mp3 = mp3
    )
}
