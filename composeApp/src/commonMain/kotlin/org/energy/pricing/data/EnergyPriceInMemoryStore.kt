package org.energy.pricing.data

import androidx.compose.runtime.mutableStateListOf

// Record representing an hourly energy price
// date_time: ISO-8601 instant string (UTC)
// price_in_kwh: price per kWh (same currency as input, typically EUR)
data class EnergyPriceRecord(
    val date_time: String,
    val price_in_kwh: Double,
)

object EnergyPriceInMemoryStore {
    val records = mutableStateListOf<EnergyPriceRecord>()

    fun clear() = records.clear()

    fun addAll(newRecords: List<EnergyPriceRecord>) {
        if (newRecords.isEmpty()) return
        records.addAll(newRecords)
    }
}
