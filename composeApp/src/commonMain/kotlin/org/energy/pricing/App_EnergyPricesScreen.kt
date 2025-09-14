package org.energy.pricing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.energy.pricing.data.EnergyPriceInMemoryStore
import org.energy.pricing.services.DateTimeService
import org.energy.pricing.services.EnergyPriceLoader

@Composable
internal fun EnergyPricesScreen() {
    var currentPage = remember { mutableStateOf(0) }
    val pageSize = 30
    val scope = rememberCoroutineScope()

    Text("Energy prices from resources", style = MaterialTheme.typography.titleMedium)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { scope.launch { EnergyPriceLoader.reload() } }) { Text("Reload prices") }
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
