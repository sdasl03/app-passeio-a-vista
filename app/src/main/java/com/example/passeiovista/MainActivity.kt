package com.example.passeiovista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.passeiovista.data.database.DatabaseProvider
import com.example.passeiovista.data.database.DatabaseSeeder
import com.example.passeiovista.data.repositories.PoiRepository
import com.example.passeiovista.ui.PasseioApp
import com.example.passeiovista.ui.theme.PasseioÀVistaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DatabaseProvider.get(this)
        val poiRepository = PoiRepository(db.poiDao())

        lifecycleScope.launch {
            DatabaseSeeder(
                categoryDao = db.categoryDao(),
                poiDao = db.poiDao(),
                favoriteDao = db.favoriteDao(),
                routeDao = db.routeDao(),
                routeItemDao = db.routeItemDao()
            ).seed()
        }

        enableEdgeToEdge()
        setContent {
            PasseioÀVistaTheme {
                PasseioApp(poiRepository = poiRepository)
            }
        }
    }
}
