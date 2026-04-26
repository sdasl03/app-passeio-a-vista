package com.example.passeiovista.data.repositories

import android.location.Location
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.model.NearbyFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository( private val favoriteDao: FavoriteDao) {

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
}