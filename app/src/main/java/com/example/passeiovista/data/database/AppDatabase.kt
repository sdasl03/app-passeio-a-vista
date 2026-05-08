package com.example.passeiovista.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RouteItemDao
import com.example.passeiovista.data.dao.RoutePoiDao
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.entity.Route
import com.example.passeiovista.data.entity.RoutePoi
import com.example.passeiovista.data.entity.RouteItem

@Database(
    entities = [Category::class, Poi::class, Favorite::class, Route::class, RoutePoi::class, RouteItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun poiDao(): PoiDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun routeDao(): RouteDao

    abstract fun routePoiDao(): RoutePoiDao
    abstract fun routeItemDao(): RouteItemDao
}