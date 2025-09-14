package org.energy.pricing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.energy.pricing.data.EnergyPriceInMemoryStore
import org.energy.pricing.services.DateTimeService
import org.energy.pricing.services.EnergyPriceLoader

@Composable
internal fun EnergyPricesScreen() {
    var currentPage = remember { mutableStateOf(0) }
    val pageSize = 30
    val scope = rememberCoroutineScope()

    val searchQuery = remember { mutableStateOf("") }
    val searchError = remember { mutableStateOf<String?>(null) }

    Text("Energy prices from resources", style = MaterialTheme.typography.titleMedium)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { scope.launch { EnergyPriceLoader.reload() } }) { Text("Reload prices") }
    }

    // Search controls
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = searchQuery.value,
            onValueChange = {
                searchQuery.value = it
                searchError.value = null
            },
            label = { Text("Search date-time (ISO or dd-MM-yyyy HH:mm)") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = {
            val normalized = normalizeToInstantString(searchQuery.value)
            if (normalized == null) {
                searchError.value = "Unrecognized date-time. Use ISO-8601 or dd-MM-yyyy HH:mm."
            } else {
                val idx = EnergyPriceInMemoryStore.records.indexOfFirst { it.date_time == normalized }
                if (idx >= 0) {
                    val newPage = idx / pageSize
                    currentPage.value = newPage
                    searchError.value = null
                } else {
                    searchError.value = "No price found for $normalized"
                }
            }
        }) { Text("Search") }
    }
    if (searchError.value != null) {
        Text(searchError.value!!, color = MaterialTheme.colorScheme.error)
    }

    val count = EnergyPriceInMemoryStore.records.size
    Text("Rows loaded: $count")


    if (count > 0) {
        val totalPages = (count + pageSize - 1) / pageSize
        if (currentPage.value >= totalPages) {
            currentPage.value = (totalPages - 1).coerceAtLeast(0)
        }
        val startIndex = currentPage.value * pageSize
        val endIndexExclusive = (startIndex + pageSize).coerceAtMost(count)

        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("date_time", modifier = Modifier.weight(1f))
            Text("price in cents/kWh", modifier = Modifier.weight(1f))
        }
        val pageItems = EnergyPriceInMemoryStore.records.subList(startIndex, endIndexExclusive)
        for (r in pageItems) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(DateTimeService.formatDutchDateTime(r.date_time), modifier = Modifier.weight(1f))
                Text(formatMilliCents(r.price_in_milli_cents_per_kwh), modifier = Modifier.weight(1f))
            }
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (currentPage.value > 0) currentPage.value -= 1 }, enabled = currentPage.value > 0) {
                Text("Previous")
            }
            Text(
                "Page ${currentPage.value + 1} of $totalPages  (showing ${startIndex + 1}–$endIndexExclusive of $count)",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { if (currentPage.value < totalPages - 1) currentPage.value += 1 },
                enabled = currentPage.value < totalPages - 1
            ) {
                Text("Next")
            }
        }
    }
}

private fun formatMilliCents(milliCents: Int): String {
    // Convert thousandths of a cent to cents with up to 3 decimals
    val negative = milliCents < 0
    val abs = kotlin.math.abs(milliCents)
    val wholeCents = abs / 1000
    val remMilli = abs % 1000
    var s = if (remMilli == 0) {
        wholeCents.toString()
    } else {
        // Always produce 3 decimals then trim trailing zeros
        val frac = remMilli.toString().padStart(3, '0').trimEnd('0')
        if (frac.isEmpty()) wholeCents.toString() else wholeCents.toString() + "." + frac
    }
    if (negative) s = "-" + s
    return s
}

private fun normalizeToInstantString(input: String): String? {
    val s = input.trim()
    if (s.isEmpty()) return null
    // Try parse as Instant
    try {
        return Instant.parse(s).toString()
    } catch (_: Exception) {}

    // Try simple Dutch format dd-MM-yyyy HH:mm assumed Europe/Amsterdam timezone
    val m = Regex("^(\\d{2})-(\\d{2})-(\\d{4})\\s+(\\d{2}):(\\d{2})").find(s) ?: return null
    val day = m.groupValues[1].toInt()
    val month = m.groupValues[2].toInt()
    val year = m.groupValues[3].toInt()
    val hour = m.groupValues[4].toInt()
    val minute = m.groupValues[5].toInt()
    return try {
        val ldt = LocalDateTime(year, month, day, hour, minute)
        ldt.toInstant(TimeZone.of("Europe/Amsterdam")).toString()
    } catch (_: Exception) {
        null
    }
}
