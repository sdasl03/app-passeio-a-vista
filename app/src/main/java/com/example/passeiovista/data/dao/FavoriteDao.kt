package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite): Long

    @Delete
    suspend fun deleteFavorite(favorite: Favorite): Int

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND poiId = :poiId LIMIT 1")
    suspend fun getFavorite(userId: String, poiId: String): Favorite?
}