package org.energy.pricing.io

import org.energy.pricing.data.ImportRecord

// Helper: parse a decimal string with up to 3 fractional digits into milli-units (Int)
private fun parseDecimalToMilli(s: String): Int? {
    val trimmed = s.trim()
    if (trimmed.isEmpty()) return null
    val negative = trimmed.startsWith("-")
    val unsigned = if (negative) trimmed.substring(1) else trimmed
    // Split on decimal point
    val parts = unsigned.split('.')
    if (parts.size > 2) return null // invalid
    val wholeStr = parts[0].ifEmpty { "0" }
    val fracStrRaw = if (parts.size == 2) parts[1] else ""
    // Keep only digits
    if (wholeStr.any { it !in '0'..'9' }) return null
    val fracDigits = fracStrRaw.take(3).padEnd(3, '0') // truncate or pad to 3
    if (fracDigits.any { it !in '0'..'9' }) return null
    val whole = wholeStr.toIntOrNull() ?: return null
    val frac = fracDigits.toIntOrNull() ?: return null
    val milli = whole * 1000 + frac
    return if (negative) -milli else milli
}

// Parse CSV content line by line, expecting at least 3 columns per line.
// We ignore the first column and store the 2nd as power_import (milli Int) and the 3rd as date_time (String).
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
        val milli = parseDecimalToMilli(second) ?: continue
        if (third.isEmpty()) continue
        out.add(ImportRecord(power_importMilli = milli, date_time = third))
    }
    return out
}

// An expected function to show a file picker to select a CSV file and read its text content.
// The result is delivered via the onResult callback. If the user cancels, onResult(null) is called.
expect fun pickCsvFileContent(onResult: (String?) -> Unit)
