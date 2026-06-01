package com.example.passeiovista.data.database

import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RoutePoiDao
import com.example.passeiovista.data.entity.Category

class DatabaseSeeder(
    private val categoryDao: CategoryDao,
    private val poiDao: PoiDao,
    private val favoriteDao: FavoriteDao,
    private val routeDao: RouteDao,
    private val routePoiDao: RoutePoiDao
) {
    suspend fun seed() {
        if (categoryDao.getCategoryById("cat_museums") == null) {
            categoryDao.insertCategory(Category(id = "cat_museums", name = "Museus"))
        }

        if (categoryDao.getCategoryById("cat_tourism") == null) {
            categoryDao.insertCategory(Category(id = "cat_tourism", name = "Turismo"))
        }
    }
}
