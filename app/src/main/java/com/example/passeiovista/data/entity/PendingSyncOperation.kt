package com.example.passeiovista.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pending_sync_operations",
    indices = [
        Index("state"),
        Index("createdAt")
    ]
)
data class PendingSyncOperation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val action: String,
    val poiId: String? = null,
    val routeId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val state: String = STATE_PENDING
) {
    companion object {
        const val STATE_PENDING = "PENDING"
        const val STATE_SYNCED = "SYNCED"
        const val STATE_FAILED = "FAILED"

        const val ACTION_FAVORITE_ADD = "FAVORITE_ADD"
        const val ACTION_FAVORITE_REMOVE = "FAVORITE_REMOVE"
        const val ACTION_ROUTE_CREATE = "ROUTE_CREATE"
        const val ACTION_ROUTE_DELETE = "ROUTE_DELETE"
    }
}
