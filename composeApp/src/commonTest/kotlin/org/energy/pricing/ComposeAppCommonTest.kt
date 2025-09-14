package org.energy.pricing

import org.energy.pricing.data.ImportRecord
import org.energy.pricing.data.InMemoryImportStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    @Test
    fun integerFixedPoint_subtraction_isExact() {
        val aMilli = 123_456 // 123.456 kWh
        val bMilli = 123_123 // 123.123 kWh
        val deltaMilli = aMilli - bMilli
        assertEquals(333, deltaMilli)
    }

    @Test
    fun inMemoryStore_computesActualUsage_exactMilli() {
        InMemoryImportStore.clear()
        val input = listOf(
            ImportRecord(power_importMilli = 123_123, date_time = "2024-01-01T00:00:00Z"),
            ImportRecord(power_importMilli = 123_456, date_time = "2024-01-01T01:00:00Z"),
        )
        InMemoryImportStore.addAll(input)
        val first = InMemoryImportStore.records.firstOrNull()
        assertTrue(first != null && first.actual_usageMilli != null, "First record and its actual_usageMilli should be present")
        assertEquals(333, first!!.actual_usageMilli)
    }
}