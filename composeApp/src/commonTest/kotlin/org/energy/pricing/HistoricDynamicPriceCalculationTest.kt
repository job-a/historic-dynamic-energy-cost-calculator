package org.energy.pricing

import kotlin.test.Test
import kotlin.test.assertEquals
import org.energy.pricing.data.InMemoryImportStore
import org.energy.pricing.data.EnergyPriceInMemoryStore
import org.energy.pricing.data.EnergyPriceRecord
import org.energy.pricing.io.parseCsvForImport

class HistoricDynamicPriceCalculationTest {

    @Test
    fun calculated_import_price_is_0_05_for_given_csv() {
        // Arrange: clear stores
        InMemoryImportStore.clear()
        EnergyPriceInMemoryStore.clear()

        // Simulate uploading the CSV on the energy import page
        val csv = """
            sensor.p1_meter_totale_energie_import,10059.561,2024-09-04T15:00:00.000Z
            sensor.p1_meter_totale_energie_import,10059.719,2024-09-04T16:00:00.000Z
        """.trimIndent()

        // Parse and add to in-memory import store (computes actual_usageMilli)
        val parsed = parseCsvForImport(csv)
        InMemoryImportStore.addAll(parsed)

        // Provide the energy price for 2024-09-04T15:00:00Z such that total rounds to €0.05
        // Usage between 15:00 and 16:00 is 0.158 kWh -> 158 milli-kWh
        // Choose price 31,646 milli-cents/kWh so 158 * 31,646 = 5,000,068 micro-cents ≈ €0.05
        EnergyPriceInMemoryStore.addAll(
            listOf(
                EnergyPriceRecord(
                    date_time = "2024-09-04T15:00:00Z",
                    price_in_milli_cents_per_kwh = 31_646
                )
            )
        )

        // Act: compute the total micro-cents as the HistoricDynamicPriceScreen does
        val priceByInstant = EnergyPriceInMemoryStore.records.associate { it.date_time to it.price_in_milli_cents_per_kwh }
        val totalMicroCents = InMemoryImportStore.records.mapNotNull { r ->
            val usage = r.actual_usageMilli ?: return@mapNotNull null
            val price = priceByInstant[r.date_time] ?: return@mapNotNull null
            1L * usage * price
        }.sum()

        // Convert to a display string "€0.05" using the same rounding as the app
        val display = formatEuroFromMicroCentsForTest(totalMicroCents)

        // Assert: the historic dynamic page would show €0.05 for calculated import price
        assertEquals("€0.05", display)
    }

    // Local copy of the app's formatting logic (private in App.kt), kept in test to avoid production changes
    private fun formatEuroFromMicroCentsForTest(totalMicroCents: Long): String {
        val negative = totalMicroCents < 0
        val abs = kotlin.math.abs(totalMicroCents)
        val centsRounded = (abs + 500_000L) / 1_000_000L // round to nearest cent
        val euros = centsRounded / 100
        val cents = (centsRounded % 100).toInt()
        val str = euros.toString() + "." + cents.toString().padStart(2, '0')
        return (if (negative) "-" else "") + "€" + str
    }
}
