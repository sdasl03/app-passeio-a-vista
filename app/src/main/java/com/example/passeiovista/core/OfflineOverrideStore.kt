package com.example.passeiovista.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OfflineOverrideStore(context: Context) {
    private val prefs = context.getSharedPreferences("passeio_a_vista_prefs", Context.MODE_PRIVATE)

    private val _forcedOffline =
        MutableStateFlow(prefs.getBoolean(KEY_FORCED_OFFLINE, false))
    val forcedOffline: StateFlow<Boolean> = _forcedOffline

    fun toggle() {
        val next = !_forcedOffline.value
        prefs.edit().putBoolean(KEY_FORCED_OFFLINE, next).apply()
        _forcedOffline.value = next
    }

    companion object {
        private const val KEY_FORCED_OFFLINE = "forced_offline"
    }
}

