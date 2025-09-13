package org.energy.pricing.importer

import org.energy.pricing.data.ImportRecord

// Parse CSV content line by line, expecting at least 3 columns per line.
// We ignore the first column and store the 2nd as power_import (Double) and the 3rd as date_time (String).
fun parseCsvForImport(content: String): List<ImportRecord> {
    val out = mutableListOf<ImportRecord>()
    val lines = content.lineSequence()
    for (rawLine in lines) {
        val line = rawLine.trim()
        if (line.isEmpty()) continue
        // Skip a potential header if present by checking if 2nd column is non-numeric
        val parts = line.split(',')
        if (parts.size < 3) continue
        val second = parts[1].trim()
        val third = parts[2].trim()
        val value = second.toDoubleOrNull() ?: continue
        if (third.isEmpty()) continue
        out.add(ImportRecord(power_import = value, date_time = third))
    }
    return out
}

// An expected function to show a file picker to select a CSV file and read its text content.
// The result is delivered via the onResult callback. If the user cancels, onResult(null) is called.
expect fun pickCsvFileContent(onResult: (String?) -> Unit)
