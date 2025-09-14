package org.energy.pricing.services

@JsFun("url => { try { var xhr = new XMLHttpRequest(); xhr.open('GET', url, false); if (xhr.overrideMimeType) { xhr.overrideMimeType('text/plain'); } xhr.send(null); if (xhr.status >= 200 && xhr.status < 300) { return xhr.responseText; } return null; } catch (e) { return null; } }")
private external fun syncGet(url: String): String?

/**
 * For Web (Wasm), we serve static resources from the dev server. We synchronously read
 * the known XML files via a synchronous XHR to avoid making the expect function suspend.
 * The Gradle config copies src/commonMain/resources into the wasmJs resources so these
 * URLs are available at runtime.
 */
actual fun listEntsoeNlXmlContents(): List<Pair<String, String>> {
    // List the known resource paths relative to the web root
    val resourcePaths = listOf(
        "entsoe/nl/2024/Energy_Prices_202401010000-202501010000.xml",
        "entsoe/nl/2025/Energy_Prices_202412312300-202512312300.xml",
    )
    val results = mutableListOf<Pair<String, String>>()
    for (fullPath in resourcePaths) {
        val text = syncGet(fullPath)
        if (text != null) {
            val rel = fullPath.removePrefix("entsoe/nl/")
            results.add(rel to text)
        }
    }
    return results.sortedBy { it.first }
}
