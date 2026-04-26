package com.example.passeiovista.data.dao

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PoiDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var poiDao: PoiDao
    private lateinit var favoriteDao: FavoriteDao

    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
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
    fun insertFavorite_retrieveNearby() {
        runBlocking {
            val category = Category(id = "cat1", name = "Histórico")
            categoryDao.insertCategory(category)

            val poi = Poi(
                id = "1",
                name = "Ribeira",
                latitude = 41.1439,
                longitude = -8.6105,
                categoryId = "cat1"
            )
            poiDao.insertPoi(poi)

            val favorite = Favorite(id = "fav1", userId = "testUser", poiId = "1")
            favoriteDao.insertFavorite(favorite)

            val proximos = favoriteDao.getFavoritesWithLocation("testUser").first()
            Assert.assertEquals(1, proximos.size)
        }
    }
}