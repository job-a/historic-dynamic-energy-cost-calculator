package org.energy.pricing.services

import java.io.File
import java.net.URLDecoder
import java.net.JarURLConnection
import java.util.jar.JarFile

actual fun listEntsoeNlXmlContents(): List<Pair<String, String>> {
    val cl = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
    val url = cl.getResource("entsoe/nl") ?: return emptyList()
    return when (url.protocol) {
        "file" -> {
            val dir = File(URLDecoder.decode(url.path, Charsets.UTF_8))
            if (!dir.exists() || !dir.isDirectory) return emptyList()
            dir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "xml" }
                .sortedBy { it.absolutePath }
                .map { file ->
                    val relPath = file.relativeTo(dir).invariantSeparatorsPath
                    relPath to file.readText()
                }.toList()
        }
        "jar" -> {
            try {
                val conn = url.openConnection()
                val jarFile: JarFile = when (conn) {
                    is JarURLConnection -> conn.jarFile
                    else -> {
                        // Fallback: parse path like "file:/.../something.jar!/entsoe/nl"
                        val path = url.path
                        val sepIdx = path.indexOf(".jar!")
                        if (sepIdx != -1) {
                            val jarPath = path.substring(0, sepIdx + 4) // include .jar
                            JarFile(URLDecoder.decode(jarPath.removePrefix("file:"), Charsets.UTF_8))
                        } else return emptyList()
                    }
                }
                val prefix = "entsoe/nl/"
                val entries = jarFile.entries()
                val results = mutableListOf<Pair<String, String>>()
                while (entries.hasMoreElements()) {
                    val e = entries.nextElement()
                    val name = e.name
                    if (!e.isDirectory && name.startsWith(prefix) && name.endsWith(".xml")) {
                        val stream = cl.getResourceAsStream(name) ?: continue
                        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val relPath = name.removePrefix(prefix)
                        results.add(relPath to text)
                    }
                }
                results.sortedBy { it.first }
            } catch (t: Throwable) {
                emptyList()
            }
        }
        else -> emptyList()
    }
}
