package com.example.passeiovista.data.repositories

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.passeiovista.data.dao.PoiDao
import com.example.passeiovista.data.dao.RouteDao
import com.example.passeiovista.data.dao.RoutePoiDao
import com.example.passeiovista.data.database.AppDatabase
import com.example.passeiovista.data.entity.Poi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RouteRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: RouteRepository

    private lateinit var routePoiDao: RoutePoiDao

    private lateinit var routeDao: RouteDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RouteRepository(
            db,
            routeDao = routeDao,
            routePoiDao = routePoiDao
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createRouteWithCorrectId() = runBlocking {
        val pois = listOf(
            Poi(id = "1", name = "Ribeira", latitude = 41.1439, longitude = -8.6105)
        )

        val routeId = repository.createRoute(
            name = "Teste",
            userId = "testUser",
            selectedPois = pois,
            startLat = 41.1439,
            startLon = -8.6105
        )

        assertTrue("ID deve ser > 0", routeId > 0)
        println("✅ Route criado com ID: $routeId")
    }
}