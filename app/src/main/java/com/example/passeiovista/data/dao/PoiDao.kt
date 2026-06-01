package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.Poi
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Query("SELECT * FROM pois ORDER BY name ASC")
    fun getAllPois(): Flow<List<Poi>>

    @Query(
        "SELECT * FROM pois WHERE latitude BETWEEN :south AND :north AND longitude BETWEEN :west AND :east ORDER BY name ASC"
    )
    fun getPoisInBounds(
        south: Double,
        north: Double,
        west: Double,
        east: Double
    ): Flow<List<Poi>>

    @Query("SELECT * FROM pois WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getPoisByCategory(categoryId: String): Flow<List<Poi>>

    @Query("SELECT * FROM pois WHERE id = :id LIMIT 1")
    suspend fun getPoiById(id: String): Poi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: Poi): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<Poi>)

    @Query("DELETE FROM pois")
    suspend fun clearPois(): Int
}
