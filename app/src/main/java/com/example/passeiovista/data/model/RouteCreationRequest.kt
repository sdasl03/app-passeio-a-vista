package com.example.passeiovista.data.model

import com.example.passeiovista.data.entity.Poi

data class RouteCreationRequest(
    val name: String,
    val userId: String,
    val pois: List<Poi>
)
