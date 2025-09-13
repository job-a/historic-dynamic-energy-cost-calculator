package org.energy.pricing.data

import androidx.compose.runtime.mutableStateListOf

// Data model for a row in the in-memory table
// We only store power_import (second csv column) and date_time (third csv column)
// Keep date_time as String (ISO-8601) for simplicity and cross-platform compatibility.
data class ImportRecord(
    val power_import: Double,
    val date_time: String,
)

// Simple in-memory table that is observable by Compose
object InMemoryStore {
    val records = mutableStateListOf<ImportRecord>()

    fun clear() = records.clear()

    fun addAll(newRecords: List<ImportRecord>) {
        records.addAll(newRecords)
    }
}
