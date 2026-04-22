package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = Poi::class,
            parentColumns = ["id"],
            childColumns = ["poiId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("poiId")]
)
data class Favorite(
    @PrimaryKey val id: String,
    val userId: String,
    val poiId: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)