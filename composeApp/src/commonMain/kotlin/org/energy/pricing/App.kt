package org.energy.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.energy.pricing.data.InMemoryStore
import org.energy.pricing.io.parseCsvForImport
import org.energy.pricing.io.pickCsvFileContent
import org.energy.pricing.services.DateTimeService
import org.energy.pricing.services.NumberService
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                    InMemoryStore.clear()
                    InMemoryStore.addAll(parsed)
                    currentPage = 0 // reset to first page after load
                }
            }
        }) {
            Text("Upload CSV…")
        }
        Button(onClick = {
            InMemoryStore.clear()
            currentPage = 0
        }) {
            Text("Clear")
        }
    }
    val count = InMemoryStore.records.size
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
        val pageItems = InMemoryStore.records.subList(startIndex, endIndexExclusive)
        for (r in pageItems) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(DateTimeService.formatDutchDateTime(r.date_time), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.power_importMilli), modifier = Modifier.weight(1f))
                Text(NumberService.formatKwh(r.actual_usageMilli), modifier = Modifier.weight(1f))
            }
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

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar with tabs
            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("Power import", "Settings")
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Divider()
            when (selectedTab) {
                0 -> PowerImportScreen()
                else -> Text("Coming soon…")
            }
        }
    }
}