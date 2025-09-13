package org.energy.pricing.data

import androidx.compose.runtime.mutableStateListOf

// Reuse the same record model for export data for now.
// power_importMilli effectively represents export meter reading in this store.
object InMemoryExportStore {
    val records = mutableStateListOf<ImportRecord>()

    fun clear() = records.clear()

    fun addAll(newRecords: List<ImportRecord>) {
        if (newRecords.isEmpty()) return
        // Compute delta between consecutive readings similar to import store
        val computed = newRecords.mapIndexed { index, current ->
            val usageMilli = if (index < newRecords.lastIndex) {
                val next = newRecords[index + 1]
                next.power_importMilli - current.power_importMilli
            } else null
            ImportRecord(
                power_importMilli = current.power_importMilli,
                date_time = current.date_time,
                actual_usageMilli = usageMilli,
            )
        }
        records.addAll(computed)
    }
}
