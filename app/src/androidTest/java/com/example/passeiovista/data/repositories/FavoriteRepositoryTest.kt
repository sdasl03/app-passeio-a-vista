package com.example.passeiovista.data.repositories

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.dao.CategoryDao
import com.example.passeiovista.data.dao.FavoriteDao
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FavoriteRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var poiDao: PoiDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: FavoriteRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("PRAGMA foreign_keys = OFF")
                }
            })
            .build()

        poiDao = db.poiDao()
        favoriteDao = db.favoriteDao()
        categoryDao = db.categoryDao()
        repository = FavoriteRepository(favoriteDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    @androidx.room.Transaction
    fun getNearbyFavoritesDistance() {
        runBlocking {
            setupTestData("testUser")

            val result = repository.getNearbyFavorites(
                "testUser",
                41.1439, -8.6105,
                1000.0
            ).first()

            assertEquals(1, result.size)
            assertEquals("Ribeira", result[0].favorite.name)
            assertTrue(result[0].distanciaMetros < 10.0)
        }
    }

    @Test
    @androidx.room.Transaction
    fun getNearbyFavoritesFilteredByRadius() {
        runBlocking {
            // 1. LIMPEZA
            favoriteDao.deleteAllFavorites("testUser")

            // 2. FK Category
            categoryDao.insertCategory(Category(id = "cat1", name = "Histórico"))

            // 3. Ribeira (0m de 41.1439, -8.6105)
            poiDao.insertPoi(Poi(id = "1", name = "Ribeira", latitude = 41.1439, longitude = -8.6105, categoryId = "cat1"))
            favoriteDao.insertFavorite(Favorite(id = "fav1", userId = "testUser", poiId = "1"))

            // 4. Gaia (4.2km!)
            poiDao.insertPoi(Poi(id = "2", name = "Gaia", latitude = 41.1185, longitude = -8.5747, categoryId = "cat1"))
            favoriteDao.insertFavorite(Favorite(id = "fav2", userId = "testUser", poiId = "2"))

            // 5. WHEN: Raio 1km
            val result = repository.getNearbyFavorites("testUser", 41.1439, -8.6105, 1000.0).first()

            // 6. THEN: 1 resultado
            assertEquals(1, result.size)  // Linha 96 ✅
            assertEquals("Ribeira", result[0].favorite.name)
        }
    }

    @Test
    @androidx.room.Transaction
    fun getNearbyFavoritesWhenEmpty() {
        runBlocking {
            favoriteDao.deleteAllFavorites()

            val result = repository.getNearbyFavorites(
                "testUser",
                41.1439, -8.6105,
                1000.0
            ).first()

            assertTrue(result.isEmpty())
        }
    }

    // HELPER FUNCTIONS (evita duplicação código)
    private suspend fun setupTestData(userId: String) {
        val category = Category(id = "cat1", name = "Histórico")
        categoryDao.insertCategory(category)

        val poi = Poi(id = "1", name = "Ribeira", latitude = 41.1439, longitude = -8.6105, categoryId = "cat1")
        poiDao.insertPoi(poi)

        val favorite = Favorite(id = "fav1_${userId}", userId = userId, poiId = "1")
        favoriteDao.insertFavorite(favorite)
    }

    private suspend fun setupTestDataWithTwoPois(userId: String) {
        val category = Category(id = "cat1", name = "Histórico")
        categoryDao.insertCategory(category)

        // Ribeira (0m)
        poiDao.insertPoi(Poi(id = "1", name = "Ribeira", latitude = 41.1439, longitude = -8.6105, categoryId = "cat1"))
        favoriteDao.insertFavorite(Favorite(id = "fav1_${userId}", userId = userId, poiId = "1"))

        // Clérigos (~1.5km)
        poiDao.insertPoi(Poi(id = "2", name = "Clérigos", latitude = 41.1470, longitude = -8.6144, categoryId = "cat1"))
        favoriteDao.insertFavorite(Favorite(id = "fav2_${userId}", userId = userId, poiId = "2"))
    }

    private suspend fun setupTestDataOrdered(userId: String) {
        val category = Category(id = "cat1", name = "Histórico")
        categoryDao.insertCategory(category)

        poiDao.insertPoi(Poi("1", "Ribeira", 41.1439, -8.6105, "cat1"))
        poiDao.insertPoi(Poi("2", "Clérigos", 41.1470, -8.6144, "cat1"))
        poiDao.insertPoi(Poi("3", "Foz", 41.1450, -8.6590, "cat1"))

        favoriteDao.insertFavorite(Favorite("fav1_${userId}", userId, "1"))
        favoriteDao.insertFavorite(Favorite("fav2_${userId}", userId, "2"))
        favoriteDao.insertFavorite(Favorite("fav3_${userId}", userId, "3"))
    }
}