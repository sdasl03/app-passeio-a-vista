package com.example.passeiovista.data.database

import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RouteItemDao
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.entity.RouteItem
import java.time.LocalDateTime
import java.util.UUID

class DatabaseSeeder(
    private val categoryDao: CategoryDao,
    private val poiDao: PoiDao,
    private val favoriteDao: FavoriteDao,
    private val routeDao: RouteDao,
    private val routeItemDao: RouteItemDao
) {
    suspend fun seed() {
        val userId = "user_demo"
        val categoryId = "cat_museums"
        val poi1Id = "poi_1"
        val poi2Id = "poi_2"
        val routeId = "route_1"

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
                    openingHours = "10:00-18:00",
                    price = "5€",
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
                    openingHours = "09:00-17:00",
                    price = "Grátis",
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
                id = UUID.randomUUID().toString(),
                userId = userId,
                poiId = poi1Id,
                createdAt = LocalDateTime.now()
            )
        )

        routeDao.insertRoute(
            Route(
                id = routeId,
                userId = userId,
                name = "Roteiro Demo",
                createdAt = LocalDateTime.now(),
                totalEstimatedTimeMinutes = 120
            )
        )

        routeItemDao.insertRouteItems(
            listOf(
                RouteItem(routeId = routeId, poiId = poi1Id, orderIndex = 1, estimatedStopMinutes = 45),
                RouteItem(routeId = routeId, poiId = poi2Id, orderIndex = 2, estimatedStopMinutes = 30)
            )
        )
    }
}