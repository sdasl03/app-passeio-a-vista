package com.example.passeiovista

import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.model.NearbyFavorite
import org.junit.Assert.*
import kotlinx.coroutines.flow.first  // <- FALTAVA!
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import androidx.room.RoomDatabase


@RunWith(AndroidJUnit4::class)
class PoiDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var poiDao: PoiDao
    private lateinit var favoriteDao: FavoriteDao

    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            // <- ADICIONA isto:
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("PRAGMA foreign_keys = OFF")  // Ignora FKs!
                }
            })
            .build()

        poiDao = db.poiDao()
        favoriteDao = db.favoriteDao()
        categoryDao = db.categoryDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    @Transaction
    fun insertFavorite_retrieveProximos() {
        runBlocking {
            val category = Category(id = "cat1", name = "Histórico")
            categoryDao.insertCategory(category)

            val poi = Poi(id = "1", name = "Ribeira", latitude = 41.1439, longitude = -8.6105)
            poiDao.insertPoi(poi)

            val favorite = Favorite(id = "fav1", userId = "testUser", poiId = "1")
            favoriteDao.insertFavorite(favorite)

            val proximos = favoriteDao.getNearbyFavorites("testUser", 41.1439, -8.6105, 10000.0).first()
            assertEquals(1, proximos.size)
        }
    }
}