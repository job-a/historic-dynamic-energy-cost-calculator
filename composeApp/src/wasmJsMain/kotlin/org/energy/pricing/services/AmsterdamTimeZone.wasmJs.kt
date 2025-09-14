package org.energy.pricing.services

import kotlinx.datetime.TimeZone

// Named time zones are not yet supported on Kotlin/Wasm JS runtime.
// Fallback to UTC to avoid IllegalTimeZoneException.
actual fun amsterdamTimeZone(): TimeZone = TimeZone.UTC
