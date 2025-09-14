package org.energy.pricing.services

/**
 * Platform-specific helper to list all XML files under resources/entsoe/nl (recursively),
 * returning pairs of (logicalPath, content).
 */
expect fun listEntsoeNlXmlContents(): List<Pair<String, String>>
