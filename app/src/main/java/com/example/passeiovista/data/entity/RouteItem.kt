package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "route_items",
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
    indices = [Index("routeId"), Index("poiId")]
)
data class RouteItem(
    val routeId: String,
    val poiId: String,
    val orderIndex: Int,
    val estimatedStopMinutes: Int = 0
)