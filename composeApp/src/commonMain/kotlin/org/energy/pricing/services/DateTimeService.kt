package org.energy.pricing.services

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Service responsible for date/time parsing and formatting.
 * Currently provides Dutch (Europe/Amsterdam) formatting.
 */
object DateTimeService {
    private val amsterdamTz = TimeZone.of("Europe/Amsterdam")

    /**
     * Formats an ISO-8601 date-time string to Dutch notation "dd-MM-yyyy HH:mm" in Europe/Amsterdam time zone.
     *
     * Parsing strategy:
     * - Try parsing as Instant first (expects offset/Z).
     * - If that fails, try parsing as LocalDateTime and assume UTC.
     * - If both fail, returns the original input unchanged.
     */
    fun formatDutchDateTime(input: String): String {
        val instant = try {
            Instant.parse(input)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(input).toInstant(TimeZone.UTC)
            } catch (e2: Exception) {
                return input
            }
        }
        val local = instant.toLocalDateTime(amsterdamTz)
        val dd = local.dayOfMonth.toString().padStart(2, '0')
        val mm = local.monthNumber.toString().padStart(2, '0')
        val yyyy = local.year.toString().padStart(4, '0')
        val HH = local.hour.toString().padStart(2, '0')
        val Min = local.minute.toString().padStart(2, '0')
        return "$dd-$mm-$yyyy $HH:$Min"
    }
}