package org.energy.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import org.energy.pricing.data.InMemoryExportStore
import org.energy.pricing.data.InMemoryImportStore
import org.energy.pricing.data.EnergyPriceInMemoryStore
import org.energy.pricing.io.parseCsvForImport
import org.energy.pricing.io.pickCsvFileContent
import org.energy.pricing.services.DateTimeService
import org.energy.pricing.services.EnergyPriceLoader
import org.energy.pricing.services.NumberService
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PowerImportScreen() {
    // Pagination state
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 30

    // CSV Upload Section
    Text("CSV Import", style = MaterialTheme.typography.titleMedium)
    Row {
        Button(onClick = {
            pickCsvFileContent { content ->
                if (content != null) {
                    val parsed = parseCsvForImport(content)
                    InMemoryImportStore.clear()
                    InMemoryImportStore.addAll(parsed)
                    currentPage = 0 // reset to first page after load
                }
            }
        }) {
            Text("Upload CSV…")
        }
        Button(onClick = {
            InMemoryImportStore.clear()
            currentPage = 0
        }) {
            Text("Clear")
        }
    }
    val count = InMemoryImportStore.records.size
    Text("Rows loaded: $count")
    if (count > 0) {
        // pagination calculations
        val totalPages = (count + pageSize - 1) / pageSize
        if (currentPage >= totalPages) {
            currentPage = (totalPages - 1).coerceAtLeast(0)
        }
        val startIndex = currentPage * pageSize
        val endIndexExclusive = (startIndex + pageSize).coerceAtMost(count)

        Divider()
        // Simple header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("date_time", modifier = Modifier.weight(1f))
            Text("power_import", modifier = Modifier.weight(1f))
            Text("actual_usage", modifier = Modifier.weight(1f))
        }
        // Show current page rows (30 per page)
        val pageItems = InMemoryImportStore.records.subList(startIndex, endIndexExclusive)
        for (r in pageItems) {
            var hovered by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (hovered) Color(0xFFE8F4FF) else Color.Transparent)
                    .onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(DateTimeService.formatDutchDateTime(r.date_time), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.power_importMilli), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.actual_usageMilli), modifier = Modifier.weight(1f))
            }
        }
        // Totals row (sum of all actual usage values across all rows)
        val totalActualUsageMilli = InMemoryImportStore.records.sumOf { it.actual_usageMilli ?: 0 }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Text("Total: " + NumberService.formatKwh(totalActualUsageMilli), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }
        // Pagination controls
        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (currentPage > 0) currentPage -= 1 }, enabled = currentPage > 0) {
                Text("Previous")
            }
            Text("Page ${currentPage + 1} of $totalPages  (showing ${startIndex + 1}–$endIndexExclusive of $count)",
                modifier = Modifier.weight(1f))
            Button(onClick = { if (currentPage < totalPages - 1) currentPage += 1 }, enabled = currentPage < totalPages - 1) {
                Text("Next")
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PowerExportScreen() {
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 30

    Text("CSV Export", style = MaterialTheme.typography.titleMedium)
    Row {
        Button(onClick = {
            pickCsvFileContent { content ->
                if (content != null) {
                    val parsed = parseCsvForImport(content)
                    InMemoryExportStore.clear()
                    InMemoryExportStore.addAll(parsed)
                    currentPage = 0
                }
            }
        }) {
            Text("Upload CSV…")
        }
        Button(onClick = {
            InMemoryExportStore.clear()
            currentPage = 0
        }) {
            Text("Clear")
        }
    }
    val count = InMemoryExportStore.records.size
    Text("Rows loaded: $count")
    if (count > 0) {
        val totalPages = (count + pageSize - 1) / pageSize
        if (currentPage >= totalPages) {
            currentPage = (totalPages - 1).coerceAtLeast(0)
        }
        val startIndex = currentPage * pageSize
        val endIndexExclusive = (startIndex + pageSize).coerceAtMost(count)

        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("date_time", modifier = Modifier.weight(1f))
            Text("power_export", modifier = Modifier.weight(1f))
            Text("actual_export", modifier = Modifier.weight(1f))
        }
        val pageItems = InMemoryExportStore.records.subList(startIndex, endIndexExclusive)
        for (r in pageItems) {
            var hovered by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (hovered) Color(0xFFE8F4FF) else Color.Transparent)
                    .onPointerEvent(PointerEventType.Enter) { hovered = true }
                    .onPointerEvent(PointerEventType.Exit) { hovered = false },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(DateTimeService.formatDutchDateTime(r.date_time), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.power_importMilli), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.actual_usageMilli), modifier = Modifier.weight(1f))
            }
        }
        // Totals row (sum of all actual export values across all rows)
        val totalActualExportMilli = InMemoryExportStore.records.sumOf { it.actual_usageMilli ?: 0 }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Text("Total: " + NumberService.formatKwh(totalActualExportMilli), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (currentPage > 0) currentPage -= 1 }, enabled = currentPage > 0) {
                Text("Previous")
            }
            Text("Page ${currentPage + 1} of $totalPages  (showing ${startIndex + 1}–$endIndexExclusive of $count)",
                modifier = Modifier.weight(1f))
            Button(onClick = { if (currentPage < totalPages - 1) currentPage += 1 }, enabled = currentPage < totalPages - 1) {
                Text("Next")
            }
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFFFAFAFA))
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Load energy prices once at startup (suspend; heavy work off main thread)
            LaunchedEffect(Unit) { EnergyPriceLoader.loadIfNeeded() }

            // Top bar with tabs
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Power import", "Power export", "Energy prices", "Historic dynamic energy price calculation")
            TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFFE7F2FF)) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = Color.Black, fontWeight = FontWeight.Bold) }
                    )
                }
            }
            Divider()
            when (selectedTab) {
                0 -> PowerImportScreen()
                1 -> PowerExportScreen()
                2 -> EnergyPricesScreen()
                3 -> HistoricDynamicPriceScreen()
                else -> PowerImportScreen()
            }
        }
    }
}
@Composable
private fun HistoricDynamicPriceScreen() {
    // Build quick lookup maps
    val priceByInstant = remember(EnergyPriceInMemoryStore.records.size) {
        EnergyPriceInMemoryStore.records.associate { it.date_time to it.price_in_milli_cents_per_kwh }
    }
    val importTotalMicroCents = remember(InMemoryImportStore.records.size, EnergyPriceInMemoryStore.records.size) {
        calcTotalMicroCents(InMemoryImportStore.records.mapNotNull { r ->
            val usage = r.actual_usageMilli ?: return@mapNotNull null
            val price = priceByInstant[r.date_time] ?: return@mapNotNull null
            1L * usage * price
        })
    }
    val exportTotalMicroCents = remember(InMemoryExportStore.records.size, EnergyPriceInMemoryStore.records.size) {
        calcTotalMicroCents(InMemoryExportStore.records.mapNotNull { r ->
            val usage = r.actual_usageMilli ?: return@mapNotNull null
            val price = priceByInstant[r.date_time] ?: return@mapNotNull null
            1L * usage * price
        })
    }

    Column(horizontalAlignment = Alignment.Start) {
        Text("Historic dynamic energy price calculation", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Calculated import price: " + formatEuroFromMicroCents(importTotalMicroCents))
        Text("Calculated export price: " + formatEuroFromMicroCents(exportTotalMicroCents))
    }
}

private fun calcTotalMicroCents(items: List<Long>): Long {
    var sum = 0L
    for (v in items) sum += v
    return sum
}

private fun formatEuroFromMicroCents(totalMicroCents: Long): String {
    // Convert micro-cents to rounded cents, then to euro string
    val negative = totalMicroCents < 0
    val abs = kotlin.math.abs(totalMicroCents)
    val centsRounded = (abs + 500_000L) / 1_000_000L // round to nearest cent
    val euros = centsRounded / 100
    val cents = (centsRounded % 100).toInt()
    val str = euros.toString() + "." + cents.toString().padStart(2, '0')
    return (if (negative) "-" else "") + "€" + str
}