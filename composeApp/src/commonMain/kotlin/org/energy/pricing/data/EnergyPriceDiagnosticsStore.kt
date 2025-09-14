package org.energy.pricing.data

import androidx.compose.runtime.mutableStateListOf

/**
 * Diagnostics messages produced during energy price loading.
 * Helps users understand what happens at startup and during manual reloads.
 */
object EnergyPriceDiagnosticsStore {
    val messages = mutableStateListOf<String>()

    fun clear() = messages.clear()

    fun add(message: String) {
        messages.add(message)
    }

    fun addAll(msgs: List<String>) {
        if (msgs.isEmpty()) return
        messages.addAll(msgs)
    }
}