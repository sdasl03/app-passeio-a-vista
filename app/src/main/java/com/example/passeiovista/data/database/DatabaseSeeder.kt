package com.example.passeiovista.data.database

import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RoutePoiDao
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.entity.RoutePoi
import java.time.LocalDateTime

class DatabaseSeeder(
    private val categoryDao: CategoryDao,
    private val poiDao: PoiDao,
    private val favoriteDao: FavoriteDao,
    private val routeDao: RouteDao,
    private val routePoiDao: RoutePoiDao
) {
    suspend fun seed() {
        val userId = "user_demo"
        val categoryId = "cat_museums"
        val poi1Id = "poi_1"
        val poi2Id = "poi_2"
        val favoriteId = "favorite_1"

        if (categoryDao.getCategoryById(categoryId) != null) return

        categoryDao.insertCategory(
            Category(
                id = categoryId,
                name = "Museus"
            )
        )

        poiDao.insertPois(
            listOf(
                Poi(
                    id = poi1Id,
                    name = "Museu de Teste 1",
                    description = "POI de teste",
                    latitude = 41.1496,
                    longitude = -8.6109,
                    address = "Porto",
                    accessibility = "Boa",
                    categoryId = categoryId,
                    isOpenNow = true,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                Poi(
                    id = poi2Id,
                    name = "Museu de Teste 2",
                    description = "Outro POI de teste",
                    latitude = 41.15,
                    longitude = -8.61,
                    address = "Gaia",
                    accessibility = "Média",
                    categoryId = categoryId,
                    isOpenNow = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            )
        )

        favoriteDao.insertFavorite(
            Favorite(
                id = favoriteId,
                userId = userId,
                poiId = poi1Id,
                createdAt = LocalDateTime.now()
            )
        )

        val routeId = routeDao.insertRoute(
            Route(
                userId = userId,
                name = "Roteiro Demo",
                createdAt = LocalDateTime.now(),
                totalEstimatedMinutes = 120,
                totalDistanceMeters = 0.0
            )
        )

        routePoiDao.insertAll(
            listOf(
                RoutePoi(routeId = routeId, poiId = poi1Id, position = 0, estimatedStopTime = 45),
                RoutePoi(routeId = routeId, poiId = poi2Id, position = 1, estimatedStopTime = 30)
            )
        )
    }
}
