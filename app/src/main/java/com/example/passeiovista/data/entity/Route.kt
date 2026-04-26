package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "routes")
data class Route(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val totalEstimatedTimeMinutes: Int = 0
)