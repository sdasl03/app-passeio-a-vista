package com.example.passeiovista.data.model

import androidx.room.Embedded
import com.example.passeiovista.data.entity.RoutePoi

data class RoutePoiWithPoi(
    @Embedded val routePoi: RoutePoi,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
