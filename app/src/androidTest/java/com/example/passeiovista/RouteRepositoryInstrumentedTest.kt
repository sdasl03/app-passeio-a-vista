package com.example.passeiovista

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Category
import com.example.passeiovista.data.entity.Poi
import com.example.passeiovista.data.repositories.RouteRepository
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouteRepositoryInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: RouteRepository

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

        repo = RouteRepository(db, db.routeDao(), db.routePoiDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun createRoute_persistsRouteAndPois() = runTest {
        val userId = "u1"
        val routeId = repo.createRoute(
            name = "Roteiro Teste",
            userId = userId,
            selectedPois = listOf(
                db.poiDao().getPoiById("p1")!!,
                db.poiDao().getPoiById("p2")!!
            ),
            startLat = 41.0,
            startLon = -8.0
        )

        val routes = repo.getRoutesByUser(userId).first()
        assertThat(routes.map { it.id }).contains(routeId)

        val routePois = repo.getRoutePois(routeId).first()
        assertThat(routePois).hasSize(2)
        assertThat(routePois.map { it.poiId }.toSet()).containsExactly("p1", "p2")
        assertThat(routePois.map { it.position }).containsExactly(0, 1)
    }

    @Test
    fun deleteRoute_deletesRouteAndRoutePois() = runTest {
        val userId = "u1"
        val routeId = repo.createRoute(
            name = "Roteiro Para Apagar",
            userId = userId,
            selectedPois = listOf(db.poiDao().getPoiById("p1")!!),
            startLat = 41.0,
            startLon = -8.0
        )

        assertThat(repo.getRoutesByUser(userId).first()).isNotEmpty()
        assertThat(repo.getRoutePois(routeId).first()).isNotEmpty()

        repo.deleteRoute(routeId)

        assertThat(repo.getRoutesByUser(userId).first()).isEmpty()
        assertThat(repo.getRoutePois(routeId).first()).isEmpty()
    }
}
