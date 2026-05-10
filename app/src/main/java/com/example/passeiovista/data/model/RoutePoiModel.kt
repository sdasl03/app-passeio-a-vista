package com.example.passeiovista.data.model

import com.example.passeiovista.data.entity.Poi

data class RoutePoiModel(
    val poi: Poi,
    val distanceFromPrevious: Double
)

