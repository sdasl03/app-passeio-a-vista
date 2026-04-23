package com.example.passeiovista.data.model

import androidx.room.ColumnInfo

data class NearbyFavorite(
    val id: String,
    val userId: String,
    val poiId: String,
    val createdAt: Long?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "distanciaMetros")  // <- MATCH exato com query alias!
    val distanciaMetros: Double  // Ou distance, mas consistente
)