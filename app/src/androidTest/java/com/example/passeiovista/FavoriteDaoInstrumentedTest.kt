package com.example.passeiovista

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Favorite
import com.example.passeiovista.data.entity.Poi
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoInstrumentedTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        db.categoryDao().insertCategory(Category(id = "c1", name = "Cat"))
        db.poiDao().insertPoi(
            Poi(
                id = "p1",
                name = "POI 1",
                latitude = 41.0,
                longitude = -8.0,
                description = "d",
                address = "a",
                accessibility = "Boa",
                categoryId = "c1",
                isOpenNow = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
        db.poiDao().insertPoi(
            Poi(
                id = "p2",
                name = "POI 2",
                latitude = 41.1,
                longitude = -8.1,
                description = "d",
                address = "a",
                accessibility = "Boa",
                categoryId = "c1",
                isOpenNow = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getFavoritesByUser_ordersByCreatedAtDesc() = runTest {
        val dao = db.favoriteDao()

        dao.insertFavorite(
            Favorite(
                id = "f1",
                userId = "u1",
                poiId = "p1",
                createdAt = LocalDateTime.of(2026, 1, 1, 10, 0)
            )
        )
        dao.insertFavorite(
            Favorite(
                id = "f2",
                userId = "u1",
                poiId = "p2",
                createdAt = LocalDateTime.of(2026, 1, 2, 10, 0)
            )
        )

        val ordered = dao.getFavoritesByUser("u1").first().map { it.id }
        assertThat(ordered).containsExactly("f2", "f1").inOrder()
    }

    @Test
    fun deleteFavorite_byUserAndPoi_removesRow() = runTest {
        val dao = db.favoriteDao()
        dao.insertFavorite(
            Favorite(
                id = "f1",
                userId = "u1",
                poiId = "p1",
                createdAt = LocalDateTime.now()
            )
        )

        assertThat(dao.getFavoritesByUser("u1").first()).hasSize(1)
        dao.deleteFavorite(userId = "u1", poiId = "p1")
        assertThat(dao.getFavoritesByUser("u1").first()).isEmpty()
    }
}
