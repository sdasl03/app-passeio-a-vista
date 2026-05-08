package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.RoutePoi
import com.example.passeiovista.data.model.RoutePoiWithPoi
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePoiDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routePois: List<RoutePoi>)

    @Query("SELECT * FROM route_pois WHERE routeId = :routeId ORDER BY position ASC")
    fun getPoisForRoute(routeId: Long): Flow<List<RoutePoi>>

    @Query("SELECT rp.*, p.name, p.latitude, p.longitude FROM route_pois rp INNER JOIN pois p ON rp.poiId = p.id WHERE rp.routeId = :routeId ORDER BY rp.position ASC")
    fun getRouteWithPois(routeId: Long): Flow<List<RoutePoiWithPoi>>

    @Query("DELETE FROM route_pois WHERE routeId = :routeId")
    suspend fun deletePoisForRoute(routeId: Long)

    @Query("UPDATE route_pois SET position = :position WHERE routeId = :routeId AND poiId = :poiId")
    suspend fun updatePosition(routeId: Long, poiId: String, position: Int)
}