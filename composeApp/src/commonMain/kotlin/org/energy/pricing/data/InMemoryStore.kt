package org.energy.pricing.data

import androidx.compose.runtime.mutableStateListOf

// Data model for a row in the in-memory table
// We only store power_import (second csv column) and date_time (third csv column)
// Keep date_time as String (ISO-8601) for simplicity and cross-platform compatibility.
// Additionally, we compute actual_usage as the difference between the next row's
// power_import and the current row's power_import. For the last row, this is null.
data class ImportRecord(
    val power_import: Double,
    val date_time: String,
    val actual_usage: Double? = null,
)

// Simple in-memory table that is observable by Compose
object InMemoryStore {
    val records = mutableStateListOf<ImportRecord>()

    fun clear() = records.clear()

    fun addAll(newRecords: List<ImportRecord>) {
        if (newRecords.isEmpty()) return
        // Compute actual_usage as next.power_import - current.power_import
        val computed = newRecords.mapIndexed { index, current ->
            val usage = if (index < newRecords.lastIndex) {
                val next = newRecords[index + 1]
                next.power_import - current.power_import
            } else null
            ImportRecord(
                power_import = current.power_import,
                date_time = current.date_time,
                actual_usage = usage,
            )
        }
        records.addAll(computed)
    }
}
