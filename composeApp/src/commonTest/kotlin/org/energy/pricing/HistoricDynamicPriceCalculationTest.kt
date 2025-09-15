package org.energy.pricing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.energy.pricing.data.InMemoryImportStore
import org.energy.pricing.data.EnergyPriceInMemoryStore
import org.energy.pricing.io.parseCsvForImport
import org.energy.pricing.services.EnergyPriceXmlParser
import org.energy.pricing.services.listEntsoeNlXmlContents

class HistoricDynamicPriceCalculationTest {

    @Test
    fun calculated_import_price_uses_prices_from_resources() {
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

        // Load energy prices from XML resources just like the application
        val files = listEntsoeNlXmlContents()
        val parsedPrices = files.flatMap { (_, text) -> EnergyPriceXmlParser.parseAll(text) }
        EnergyPriceInMemoryStore.addAll(parsedPrices)

        // Act: compute the total micro-cents as the HistoricDynamicPriceScreen does
        val priceByInstant = EnergyPriceInMemoryStore.records.associate { it.date_time to it.price_in_milli_cents_per_kwh }
        val totalMicroCents = InMemoryImportStore.records.mapNotNull { r ->
            val usage = r.actual_usageMilli ?: return@mapNotNull null
            val price = priceByInstant[r.date_time] ?: return@mapNotNull null
            1L * usage * price
        }.sum()
        val display = formatEuroFromMicroCentsForTest(totalMicroCents)

        // Compute expected from the specific hour's price to ensure we truly used resource prices
        val hourPrice = priceByInstant["2024-09-04T15:00:00Z"]
        assertNotNull(hourPrice, "Expected to find a price for 2024-09-04T15:00:00Z in XML resources")
        val expectedMicroCents = 1L * 158 /* milli-kWh */ * hourPrice
        val expectedDisplay = formatEuroFromMicroCentsForTest(expectedMicroCents)

        // Assert: calculated value equals the one derived from resource prices
        assertEquals(expectedDisplay, display)
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
