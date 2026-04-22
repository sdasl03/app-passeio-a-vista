package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pois",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Poi(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val openingHours: String?,
    val price: String?,
    val accessibility: String,
    val categoryId: String,
    val isOpenNow: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)