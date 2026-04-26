package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.RouteItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteItem(item: RouteItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteItems(items: List<RouteItem>): List<Long>

    @Query("SELECT * FROM route_items WHERE routeId = :routeId ORDER BY orderIndex ASC")
    fun getItemsByRoute(routeId: String): Flow<List<RouteItem>>

    @Query("DELETE FROM route_items WHERE routeId = :routeId")
    suspend fun deleteItemsByRoute(routeId: String): Int
}