package com.example.passeiovista.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passeiovista.data.entity.PendingSyncOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncOperationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(operation: PendingSyncOperation): Long

    @Query(
        "SELECT COUNT(*) FROM pending_sync_operations WHERE state = :state"
    )
    fun observeCountByState(state: String): Flow<Int>

    @Query(
        "SELECT * FROM pending_sync_operations WHERE state = :state ORDER BY createdAt ASC"
    )
    fun observeByState(state: String): Flow<List<PendingSyncOperation>>

    @Query("UPDATE pending_sync_operations SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: String)

    @Query("DELETE FROM pending_sync_operations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
