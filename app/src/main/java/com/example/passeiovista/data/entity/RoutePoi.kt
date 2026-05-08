package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "route_pois",
    primaryKeys = ["routeId", "poiId"],
    foreignKeys = [
        ForeignKey(
            entity = Route::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Poi::class,
            parentColumns = ["id"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routeId"),
        Index("poiId")
    ]
)
data class RoutePoi(
    val routeId: Long,
    val poiId: String,
    val position: Int,
    val estimatedStopTime: Int = 0
)