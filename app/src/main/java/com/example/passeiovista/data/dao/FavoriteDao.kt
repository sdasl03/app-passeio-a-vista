package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.model.NearbyFavorite
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

    @Query("""
    SELECT f.*, p.name, p.latitude, p.longitude,
           (6371000 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * 
                           cos(radians(p.longitude) - radians(:lon)) + 
                           sin(radians(:lat)) * sin(radians(p.latitude)))) AS distanciaMetros
    FROM favorites f 
    INNER JOIN pois p ON f.poiId = p.id 
    WHERE f.userId = :userId 
      AND (6371000 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * 
                           cos(radians(p.longitude) - radians(:lon)) + 
                           sin(radians(:lat)) * sin(radians(p.latitude)))) < :maxDistance
    ORDER BY distanciaMetros
""")
    fun getNearbyFavorites(
        userId: String, lat: Double, lon: Double, maxDistance: Double  // metros
    ): Flow<List<NearbyFavorite>>
}

