package com.example.passeiovista.data.model


data class FavoriteWithLocation(
    val id: String,
    val userId: String,
    val poiId: String,
    val createdAt: Long?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)