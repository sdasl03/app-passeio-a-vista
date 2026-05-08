package com.example.passeiovista.data.model

data class RouteSummary(
    val orderedPois: List<RoutePoiModel>,
    val totalDistanceMeters: Double,
    val totalEstimatedMinutes: Int
)