package dev.boardmydelulu.anupam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT soundId FROM favorites")
    fun getAllIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE soundId = :soundId)")
    fun isFavorite(soundId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE soundId = :soundId")
    suspend fun deleteById(soundId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM favorites")
    fun count(): Flow<Int>
}
