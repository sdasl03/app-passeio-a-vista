package com.example.passeiovista.data.repositories

import android.location.Location
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.model.FavoriteWithLocation
import com.example.passeiovista.data.model.NearbyFavorite
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(
    private val favoriteDao: FavoriteDao,
    private val pendingSyncRepository: PendingSyncRepository
) {

    fun getFavoritesByUser(
        userId: String
    ): Flow<List<Favorite>> = favoriteDao.getFavoritesByUser(userId)

    fun getFavoritePoiIds(
        userId: String
    ): Flow<Set<String>> = favoriteDao.getFavoritesByUser(userId)
        .map { favorites -> favorites.map { it.poiId }.toSet() }

    fun getFavoritesWithLocation(
        userId: String
    ): Flow<List<FavoriteWithLocation>> = favoriteDao.getFavoritesWithLocation(userId)

    suspend fun addFavorite(
        userId: String,
        poiId: String
    ) {
        favoriteDao.insertFavorite(
            Favorite(
                id = favoriteId(userId, poiId),
                userId = userId,
                poiId = poiId,
                createdAt = LocalDateTime.now()
            )
        )
        pendingSyncRepository.enqueueFavoriteAdd(userId = userId, poiId = poiId)
    }

    suspend fun removeFavorite(
        userId: String,
        poiId: String
    ) {
        favoriteDao.deleteFavorite(userId = userId, poiId = poiId)
        pendingSyncRepository.enqueueFavoriteRemove(userId = userId, poiId = poiId)
    }

    suspend fun toggleFavorite(
        userId: String,
        poiId: String
    ): Boolean {
        val existing = favoriteDao.getFavorite(userId = userId, poiId = poiId)
        return if (existing == null) {
            addFavorite(userId = userId, poiId = poiId)
            true
        } else {
            removeFavorite(userId = userId, poiId = poiId)
            false
        }
    }

    fun getNearbyFavorites(
        userId: String,
        userLat: Double,
        userLon: Double,
        maxDistance: Double
    ): Flow<List<NearbyFavorite>> = favoriteDao.getFavoritesWithLocation(userId)
        .map { favorites ->
        favorites.map { fav ->
            val distance = calculateDistance(
                userLat, userLon,
                fav.latitude, fav.longitude
            )
            NearbyFavorite(fav,distance)
        }
            .filter { it.distanciaMetros <= maxDistance }
            .sortedBy { it.distanciaMetros }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()  // Metros
    }

    private fun favoriteId(userId: String, poiId: String): String = "fav_${userId}_$poiId"
}
