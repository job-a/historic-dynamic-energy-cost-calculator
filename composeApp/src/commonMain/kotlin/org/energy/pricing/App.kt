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
import org.energy.pricing.importer.parseCsvForImport
import org.energy.pricing.importer.pickCsvFileContent
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun PowerImportScreen() {
    // CSV Upload Section
    Text("CSV Import", style = MaterialTheme.typography.titleMedium)
    Row {
        Button(onClick = {
            pickCsvFileContent { content ->
                if (content != null) {
                    val parsed = parseCsvForImport(content)
                    InMemoryStore.clear()
                    InMemoryStore.addAll(parsed)
                }
            }
        }) {
            Text("Upload CSV…")
        }
        Button(onClick = { InMemoryStore.clear() }) {
            Text("Clear")
        }
    }
    val count = InMemoryStore.records.size
    Text("Rows loaded: $count")
    if (count > 0) {
        Divider()
        // Simple header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("date_time", modifier = Modifier.weight(1f))
            Text("power_import", modifier = Modifier.weight(1f))
            Text("actual_usage", modifier = Modifier.weight(1f))
        }
        // Show up to first 10 rows
        val preview = InMemoryStore.records.take(10)
        for (r in preview) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(r.date_time, modifier = Modifier.weight(1f))
                Text(r.power_import.toString(), modifier = Modifier.weight(1f))
                Text(r.actual_usage?.toString() ?: "", modifier = Modifier.weight(1f))
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