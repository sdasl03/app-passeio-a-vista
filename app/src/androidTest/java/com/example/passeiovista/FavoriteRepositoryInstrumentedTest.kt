package com.example.passeiovista

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.example.passeiovista.data.repositories.PendingSyncRepository
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteRepositoryInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: FavoriteRepository
    private lateinit var pendingSyncRepository: PendingSyncRepository

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
        pendingSyncRepository =
            PendingSyncRepository(db.pendingSyncOperationDao(), MutableStateFlow(true))
        repo = FavoriteRepository(db.favoriteDao(), pendingSyncRepository)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun toggleFavorite_addThenRemove_updatesFlow() = runTest {
        val userId = "u1"
        val poiId = "p1"

        assertThat(repo.getFavoritePoiIds(userId).first()).isEmpty()
        assertThat(pendingSyncRepository.pendingCount.first()).isEqualTo(0)
        assertThat(repo.toggleFavorite(userId, poiId)).isTrue()
        assertThat(repo.getFavoritePoiIds(userId).first()).containsExactly(poiId)
        assertThat(pendingSyncRepository.pendingCount.first()).isEqualTo(1)
        assertThat(repo.toggleFavorite(userId, poiId)).isFalse()
        assertThat(repo.getFavoritePoiIds(userId).first()).isEmpty()
        assertThat(pendingSyncRepository.pendingCount.first()).isEqualTo(2)
    }
}
