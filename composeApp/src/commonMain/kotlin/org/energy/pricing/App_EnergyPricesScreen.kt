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
import org.energy.pricing.data.EnergyPriceDiagnosticsStore
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
        Button(onClick = { EnergyPriceDiagnosticsStore.clear() }) { Text("Clear diagnostics") }
    }

    val count = EnergyPriceInMemoryStore.records.size
    Text("Rows loaded: $count")

    // Diagnostics section (always visible to offer hints)
    Divider()
    Text("Loader diagnostics:", style = MaterialTheme.typography.titleSmall)
    Column {
        if (EnergyPriceDiagnosticsStore.messages.isEmpty()) {
            Text("(no diagnostics yet)")
        } else {
            EnergyPriceDiagnosticsStore.messages.forEach { msg ->
                Text(msg)
            }
        }
    }

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
            Text("price_in_kwh", modifier = Modifier.weight(1f))
        }
        val pageItems = EnergyPriceInMemoryStore.records.subList(startIndex, endIndexExclusive)
        for (r in pageItems) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(DateTimeService.formatDutchDateTime(r.date_time), modifier = Modifier.weight(1f))
                Text(formatPrice(r.price_in_kwh), modifier = Modifier.weight(1f))
            }
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (currentPage.value > 0) currentPage.value -= 1 }, enabled = currentPage.value > 0) {
                Text("Previous")
            }
            Text(
                "Page ${'$'}{currentPage.value + 1} of ${'$'}totalPages  (showing ${'$'}{startIndex + 1}–${'$'}endIndexExclusive of ${'$'}count)",
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

private fun formatPrice(value: Double): String {
    // Show up to 4 decimals without relying on platform-specific formatting APIs
    val scaled = kotlin.math.round(value * 10_000.0) / 10_000.0
    // Ensure a dot as decimal separator
    val s = scaled.toString()
    // Optionally pad to 4 decimals for consistent look
    val idx = s.indexOf('.')
    return if (idx == -1) s + ".0000" else (s + "0000").substring(0, idx + 1 + 4)
}
