package com.example.passeiovista.data.repositories

import com.example.passeiovista.data.dao.PendingSyncOperationDao
import com.example.passeiovista.data.entity.PendingSyncOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PendingSyncRepository(
    private val dao: PendingSyncOperationDao,
    private val isOffline: Flow<Boolean>
) {
    val pendingCount: Flow<Int> = dao.observeCountByState(PendingSyncOperation.STATE_PENDING)

    val pendingOperations: Flow<List<PendingSyncOperation>> =
        dao.observeByState(PendingSyncOperation.STATE_PENDING)

    suspend fun enqueueIfOffline(operation: PendingSyncOperation) {
        if (!isOffline.first()) return
        dao.insert(operation)
    }

    suspend fun enqueueFavoriteAdd(userId: String, poiId: String) {
        enqueueIfOffline(
            PendingSyncOperation(
                userId = userId,
                action = PendingSyncOperation.ACTION_FAVORITE_ADD,
                poiId = poiId
            )
        )
    }

    suspend fun enqueueFavoriteRemove(userId: String, poiId: String) {
        enqueueIfOffline(
            PendingSyncOperation(
                userId = userId,
                action = PendingSyncOperation.ACTION_FAVORITE_REMOVE,
                poiId = poiId
            )
        )
    }

    suspend fun enqueueRouteCreate(userId: String, routeId: Long) {
        enqueueIfOffline(
            PendingSyncOperation(
                userId = userId,
                action = PendingSyncOperation.ACTION_ROUTE_CREATE,
                routeId = routeId
            )
        )
    }

    suspend fun enqueueRouteDelete(userId: String, routeId: Long) {
        enqueueIfOffline(
            PendingSyncOperation(
                userId = userId,
                action = PendingSyncOperation.ACTION_ROUTE_DELETE,
                routeId = routeId
            )
        )
    }
}
