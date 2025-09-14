package org.energy.pricing.io

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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

// Normalize various date-time input formats to canonical Instant.toString() (UTC with seconds).
private fun normalizeToInstantString(input: String): String? {
    val s = input.trim()
    if (s.isEmpty()) return null
    // 1) Try strict Instant
    try { return Instant.parse(s).toString() } catch (_: Exception) {}
    // 2) Handle no-seconds ISO forms like 2023-12-31T23:00Z or with offset
    val noSecondsZ = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}Z$").matches(s)
    val noSecondsOffset = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$").matches(s)
    if (noSecondsZ || noSecondsOffset) {
        val normalized = if (noSecondsZ) s.replace("Z", ":00Z") else s.replace(Regex("([+-]\\d{2}:\\d{2})$"), ":00$1")
        try { return Instant.parse(normalized).toString() } catch (_: Exception) {}
    }
    // 3) Try Dutch format dd-MM-yyyy HH:mm assuming Europe/Amsterdam local time
    val m = Regex("^(\\d{2})-(\\d{2})-(\\d{4})\\s+(\\d{2}):(\\d{2})$").find(s)
    if (m != null) {
        val day = m.groupValues[1].toInt()
        val month = m.groupValues[2].toInt()
        val year = m.groupValues[3].toInt()
        val hour = m.groupValues[4].toInt()
        val minute = m.groupValues[5].toInt()
        try {
            val ldt = LocalDateTime(year, month, day, hour, minute)
            return ldt.toInstant(TimeZone.of("Europe/Amsterdam")).toString()
        } catch (_: Exception) {}
    }
    return null
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
        val instantStr = normalizeToInstantString(third) ?: continue
        out.add(ImportRecord(power_importMilli = milli, date_time = instantStr))
    }
    return out
}

// An expected function to show a file picker to select a CSV file and read its text content.
// The result is delivered via the onResult callback. If the user cancels, onResult(null) is called.
expect fun pickCsvFileContent(onResult: (String?) -> Unit)
