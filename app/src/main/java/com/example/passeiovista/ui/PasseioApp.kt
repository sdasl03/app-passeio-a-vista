package com.example.passeiovista.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.passeiovista.data.model.FavoriteWithLocation
import com.example.passeiovista.data.repositories.FavoriteRepository
import com.example.passeiovista.data.repositories.PoiRepository
import com.example.passeiovista.ui.screens.FavoritesSheet
import com.example.passeiovista.ui.screens.MapScreen

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Data(val favorites: List<FavoriteWithLocation>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasseioApp(
    poiRepository: PoiRepository,
    favoriteRepository: FavoriteRepository,
    userId: String,
    modifier: Modifier = Modifier
) {
    val pois by poiRepository.getAllPois().collectAsState(initial = emptyList())

    var showFavorites by remember { mutableStateOf(false) }
    val favoritesUiState by produceState<FavoritesUiState>(
        initialValue = FavoritesUiState.Loading,
        key1 = favoriteRepository,
        key2 = userId
    ) {
        try {
            favoriteRepository.getFavoritesWithLocation(userId).collect { favorites ->
                value = FavoritesUiState.Data(favorites)
            }
        } catch (t: Throwable) {
            value = FavoritesUiState.Error(t.message ?: "Erro ao carregar favoritos")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Passeio à Vista",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { showFavorites = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Favoritos"
                        )
                    }
                }
            )
        }
    ) { padding ->
        MapScreen(
            pois = pois,
            favoriteRepository = favoriteRepository,
            userId = userId,
            modifier = Modifier.padding(padding)
        )
    }

    if (showFavorites) {
        FavoritesSheet(
            state = favoritesUiState,
            onDismissRequest = { showFavorites = false }
        )
    }
}
