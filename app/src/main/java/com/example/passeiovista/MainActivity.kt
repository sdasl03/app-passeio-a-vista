package com.example.passeiovista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.passeiovista.core.NetworkMonitor
import com.example.passeiovista.core.OfflineOverrideStore
import com.example.passeiovista.core.OfflineStatusStore
import com.example.passeiovista.data.database.DatabaseProvider
import com.example.passeiovista.data.database.DatabaseSeeder
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.example.passeiovista.data.repositories.PendingSyncRepository
import com.example.passeiovista.data.repositories.PoiRepository
import com.example.passeiovista.data.repositories.RouteRepository
import com.example.passeiovista.data.repositories.TourismPoiRemoteRepository
import com.example.passeiovista.ui.PasseioApp
import com.example.passeiovista.ui.theme.PasseioÀVistaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_PasseioÀVista)
        super.onCreate(savedInstanceState)
        val db = DatabaseProvider.get(this)
        db.openHelper.writableDatabase.execSQL(
            "INSERT OR IGNORE INTO categories(id, name) VALUES('${TourismPoiRemoteRepository.TOURISM_CATEGORY_ID}', 'Turismo')"
        )
        val poiRepository = PoiRepository(db.poiDao())
        val tourismPoiRemoteRepository = TourismPoiRemoteRepository(db.poiDao())
        val userId = "user_demo"
        val networkMonitor = NetworkMonitor(applicationContext)
        val offlineOverrideStore = OfflineOverrideStore(applicationContext)
        val offlineStatusStore = OfflineStatusStore(networkMonitor, offlineOverrideStore)
        val pendingSyncRepository =
            PendingSyncRepository(db.pendingSyncOperationDao(), offlineStatusStore.isOffline)
        val favoriteRepository = FavoriteRepository(db.favoriteDao(), pendingSyncRepository)
        val routeRepository = RouteRepository(db, db.routeDao(), db.routePoiDao(), pendingSyncRepository)

        lifecycleScope.launch {
            DatabaseSeeder(
                categoryDao = db.categoryDao(),
                poiDao = db.poiDao(),
                favoriteDao = db.favoriteDao(),
                routeDao = db.routeDao(),
                routePoiDao = db.routePoiDao()
            ).seed()

            try {
                tourismPoiRemoteRepository.refreshTourismPoisInBounds(
                    south = 41.12,
                    north = 41.20,
                    west = -8.70,
                    east = -8.55
                )
            } catch (_: Throwable) {
            }
        }

        enableEdgeToEdge()
        setContent {
            PasseioÀVistaTheme {
                PasseioApp(
                    poiRepository = poiRepository,
                    tourismPoiRemoteRepository = tourismPoiRemoteRepository,
                    favoriteRepository = favoriteRepository,
                    routeRepository = routeRepository,
                    userId = userId,
                    isOffline = offlineStatusStore.isOffline,
                    forcedOffline = offlineOverrideStore.forcedOffline,
                    onToggleForcedOffline = { offlineOverrideStore.toggle() },
                    pendingSyncCount = pendingSyncRepository.pendingCount
                )
            }
        }
    }
}
