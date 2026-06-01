package com.example.passeiovista.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class OfflineStatusStore(
    networkMonitor: NetworkMonitor,
    offlineOverrideStore: OfflineOverrideStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val isOffline: StateFlow<Boolean> = combine(
        networkMonitor.isOnline,
        offlineOverrideStore.forcedOffline
    ) { online, forcedOffline ->
        forcedOffline || !online
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )
}

