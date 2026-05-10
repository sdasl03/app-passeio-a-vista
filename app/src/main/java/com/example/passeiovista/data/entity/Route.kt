package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val totalEstimatedMinutes: Int = 0,
    val totalDistanceMeters: Double = 0.0
)
