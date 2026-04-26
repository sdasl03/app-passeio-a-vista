package com.example.passeiovista.data.repositories

import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.entity.Poi
import kotlinx.coroutines.flow.Flow

class PoiRepository(
    private val poiDao: PoiDao
) {
    fun getAllPois(): Flow<List<Poi>> = poiDao.getAllPois()

    fun getPoisByCategory(categoryId: String): Flow<List<Poi>> =
        poiDao.getPoisByCategory(categoryId)

    suspend fun getPoiById(id: String): Poi? = poiDao.getPoiById(id)

    suspend fun insertPoi(poi: Poi) = poiDao.insertPoi(poi)

    suspend fun insertPois(pois: List<Poi>) = poiDao.insertPois(pois)

    suspend fun clearPois() = poiDao.clearPois()
}