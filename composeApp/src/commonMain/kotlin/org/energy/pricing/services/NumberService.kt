package org.energy.pricing.services

/**
 * Simple number formatting utilities for the UI.
 */
object NumberService {
    /**
     * Formats a milli-kWh integer into a kWh string with up to 3 decimals and appends " kWh".
     * - Trailing zeros and trailing decimal separator are trimmed.
     * - Null input returns empty string.
     */
    fun formatKwh(milli: Int?): String {
        if (milli == null) return ""
        val negative = milli < 0
        val absMilli = kotlin.math.abs(milli)
        val whole = absMilli / 1000
        val frac = absMilli % 1000
        var fracStr = frac.toString().padStart(3, '0')
        // Trim trailing zeros
        fracStr = fracStr.trimEnd('0')
        val number = if (fracStr.isEmpty()) {
            whole.toString()
        } else {
            "$whole.$fracStr"
        }
        return (if (negative) "-" else "") + number + " kWh"
    }
}
