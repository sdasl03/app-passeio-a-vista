package com.example.passeiovista.data.model

import com.example.passeiovista.data.entity.RoutePoi

data class RoutePoiWithPoi(
    val routePoi: RoutePoi,
    val name: String,
    val latitude: Double,
    val longitude: Double
)