package org.energy.pricing.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.energy.pricing.data.EnergyPriceDiagnosticsStore
import org.energy.pricing.data.EnergyPriceInMemoryStore

object EnergyPriceLoader {
    private var loaded = false

    suspend fun loadIfNeeded() {
        if (loaded) return
        val logs = mutableListOf<String>()
        logs += "Starting energy price load…"
        // Do heavy IO and parsing off the main thread
        val result: List<org.energy.pricing.data.EnergyPriceRecord> = try {
            withContext(Dispatchers.Default) {
                val all = mutableListOf<org.energy.pricing.data.EnergyPriceRecord>()
                val files = listEntsoeNlXmlContents()
                logs += "Found ${files.size} resource file(s) under entsoe/nl."
                for ((path, text) in files) {
                    logs += "Parsing $path…"
                    val parsed = EnergyPriceXmlParser.parseAll(text)
                    logs += "Parsed ${parsed.size} hourly price(s) from $path."
                    all.addAll(parsed)
                }
                // Sort and deduplicate by date_time keeping the last value found
                val map = linkedMapOf<String, org.energy.pricing.data.EnergyPriceRecord>()
                for (rec in all.sortedBy { it.date_time }) {
                    map[rec.date_time] = rec
                }
                map.values.toList()
            }
        } catch (t: Throwable) {
            logs += "Error during energy price load: ${t.message ?: t.toString()}"
            emptyList()
        }
        // Update Compose state on caller context (LaunchedEffect uses Main)
        EnergyPriceInMemoryStore.clear()
        EnergyPriceInMemoryStore.addAll(result)
        logs += "Loaded ${result.size} total hourly price records."
        // Publish diagnostics (on main thread / caller context)
        EnergyPriceDiagnosticsStore.clear()
        EnergyPriceDiagnosticsStore.addAll(logs)
        // Also print to console for JVM users
        logs.forEach { println("[EnergyPriceLoader] $it") }
        loaded = true
    }

    suspend fun reload() {
        loaded = false
        loadIfNeeded()
    }
}
