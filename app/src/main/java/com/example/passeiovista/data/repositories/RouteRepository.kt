package com.example.passeiovista.data.repositories

import android.location.Location
import androidx.room.withTransaction
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RoutePoiDao
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.entity.RoutePoi
import com.example.passeiovista.data.model.RoutePoiModel

import com.example.passeiovista.data.model.RouteSummary
import kotlinx.coroutines.flow.Flow

class RouteRepository(private val db: AppDatabase,
    private val routeDao: RouteDao, private val routePoiDao: RoutePoiDao
) {
    fun buildRouteSummary(
        selectedPois: List<Poi>,
        startLat: Double,
        startLon: Double
    ): RouteSummary {
        if (selectedPois.isEmpty()) {
            return RouteSummary(
                orderedPois = emptyList(),
                totalDistanceMeters = 0.0,
                totalEstimatedMinutes = 0
            )
        }

        val poisWithDistance = selectedPois.map { poi ->
            val results = FloatArray(1)
            Location.distanceBetween(
                startLat,
                startLon,
                poi.latitude,
                poi.longitude,
                results
            )

            poi to results[0].toDouble()
        }.sortedBy { it.second }

        val orderedPois = poisWithDistance.map { (poi, distance) ->
            RoutePoiModel(
                poi = poi,
                distanceFromPrevious = distance
            )
        }

        val totalDistanceMeters = poisWithDistance.sumOf { it.second }
        val totalEstimatedMinutes = estimateWalkingMinutes(totalDistanceMeters)

        return RouteSummary(
            orderedPois = orderedPois,
            totalDistanceMeters = totalDistanceMeters,
            totalEstimatedMinutes = totalEstimatedMinutes
        )
    }

    private fun estimateWalkingMinutes(distanceMeters: Double): Int {
        val walkingSpeedMetersPerMinute = 83.33
        return (distanceMeters / walkingSpeedMetersPerMinute).toInt()
    }

    suspend fun createRoute(
        name: String,
        userId: String,
        selectedPois: List<Poi>,
        startLat: Double,
        startLon: Double
    ): Long {
        val summary = buildRouteSummary(selectedPois, startLat, startLon)

        return db.withTransaction {
            val routeId = routeDao.insertRoute(
                Route(
                    name = name,
                    userId = userId,
                    totalDistanceMeters = summary.totalDistanceMeters,
                    totalEstimatedMinutes = summary.totalEstimatedMinutes
                )
            )

            val routePois = summary.orderedPois.mapIndexed { index, item ->
                RoutePoi(
                    routeId = routeId,
                    poiId = item.poi.id,
                    position = index,
                    estimatedStopTime = 0
                )
            }

            routePoiDao.insertAll(routePois)
            routeId
        }
    }

    suspend fun updateRoute(
        routeId: Long,
        name: String,
        userId: String,
        selectedPois: List<Poi>,
        startLat: Double,
        startLon: Double
    ) {
        val summary = buildRouteSummary(selectedPois, startLat, startLon)

        db.withTransaction {
            routePoiDao.deletePoisForRoute(routeId)

            routeDao.updateRoute(
                Route(
                    id = routeId,
                    userId = userId,
                    name = name,
                    totalDistanceMeters = summary.totalDistanceMeters,
                    totalEstimatedMinutes = summary.totalEstimatedMinutes
                )
            )

            routePoiDao.insertAll(
                summary.orderedPois.mapIndexed { index, item ->
                    RoutePoi(
                        routeId = routeId,
                        poiId = item.poi.id,
                        position = index,
                        estimatedStopTime = 0
                    )
                }
            )
        }
    }

    fun getRoutesByUser(userId: String): Flow<List<Route>> = routeDao.getRoutesByUser(userId)

    fun getRoutePois(routeId: Long): Flow<List<RoutePoi>> = routePoiDao.getPoisForRoute(routeId)

    suspend fun deleteRoute(routeId: Long) {
        db.withTransaction {
            routePoiDao.deletePoisForRoute(routeId)
            routeDao.deleteRouteById(routeId)
        }
    }

}
