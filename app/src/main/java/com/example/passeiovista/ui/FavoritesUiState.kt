package com.example.passeiovista.ui

import com.example.passeiovista.data.model.FavoriteWithLocation

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Data(val favorites: List<FavoriteWithLocation>) : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}