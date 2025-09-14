package org.energy.pricing.services

import kotlinx.datetime.TimeZone

// Provides the Amsterdam time zone in a platform-safe way.
// On platforms where named zones are unsupported (e.g., Wasm/JS),
// the implementation may fall back to UTC to avoid crashes.
expect fun amsterdamTimeZone(): TimeZone
