package org.energy.pricing.services

import kotlinx.datetime.Instant
import org.energy.pricing.data.EnergyPriceRecord

/**
 * Very lightweight XML parser tailored for ENTSO-E price documents.
 * It looks for Period blocks, extracts the timeInterval start/end
 * and the list of Point/position/price.amount within each Period.
 * For each point, we compute the hour start time as start + (position-1) hours and convert price MWh -> kWh.
 * Then we convert EUR/kWh to thousandths of a cent per kWh (rounded to nearest 0.001 cent).
 *
 * Notes:
 * - Tags may be namespace-qualified (e.g., ns0:Period). We match with an optional prefix.
 * - price.amount contains a dot in the tag name; we escape it in regex so it is treated literally.
 */
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

object EnergyPriceXmlParser {
    private const val NS_OPT = "(?:[A-Za-z0-9_]+:)?" // optional namespace prefix like ns0:

    private fun parseInstantFlexible(value: String): Instant? {
        val s = value.trim()
        // Try strict parser first
        try { return Instant.parse(s) } catch (_: Exception) {}
        // If time has no seconds like 2023-12-31T23:00Z or 2023-12-31T23:00+01:00, insert :00 seconds
        val noSecondsZ = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}Z").matches(s)
        val noSecondsOffset = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}").matches(s)
        val normalized = when {
            noSecondsZ -> s.replace("Z", ":00Z")
            noSecondsOffset -> s.replace(Regex("([+-]\\d{2}:\\d{2})$"), ":00$1")
            else -> null
        }
        if (normalized != null) {
            try { return Instant.parse(normalized) } catch (_: Exception) {}
        }
        return null
    }

    fun parseAll(content: String): List<EnergyPriceRecord> {
        val results = mutableListOf<EnergyPriceRecord>()
        // Match <(ns:)Period ...> ... </(ns:)Period>
        val periodRegex = Regex("(?s)<${NS_OPT}Period\\b[^>]*>(.*?)</${NS_OPT}Period>")
        val periods = periodRegex.findAll(content)
        for (pMatch in periods) {
            val block = pMatch.groupValues[1]
            val start = Regex("(?s)<${NS_OPT}timeInterval\\b[^>]*>\\s*<${NS_OPT}start>(.*?)</${NS_OPT}start>")
                .find(block)?.groupValues?.getOrNull(1) ?: continue
            // val end may be useful for validation but is not required to compute hour positions
            val points = Regex("(?s)<${NS_OPT}Point\\b[^>]*>(.*?)</${NS_OPT}Point>").findAll(block)
            val startInstant = parseInstantFlexible(start) ?: continue
            for (pm in points) {
                val pBlock = pm.groupValues[1]
                val posStr = Regex("<${NS_OPT}position>(.*?)</${NS_OPT}position>")
                    .find(pBlock)?.groupValues?.getOrNull(1)
                val priceStr = Regex("<${NS_OPT}price\\.amount>(.*?)</${NS_OPT}price\\.amount>")
                    .find(pBlock)?.groupValues?.getOrNull(1)
                val position = posStr?.trim()?.toIntOrNull() ?: continue
                val priceMwh = priceStr?.trim()?.replace(",", ".")?.toDoubleOrNull() ?: continue
                val hourInstant = try {
                    startInstant.plus((position - 1).hours)
                } catch (e: Exception) { continue }
                val priceKwhEur = priceMwh / 1000.0
                val milliCentsPerKwh = (priceKwhEur * 100_000.0).roundToInt()
                results.add(
                    EnergyPriceRecord(
                        date_time = hourInstant.toString(),
                        price_in_milli_cents_per_kwh = milliCentsPerKwh,
                    )
                )
            }
        }
        // Sort results by date_time to have deterministic order
        return results.sortedBy { it.date_time }
    }
}
