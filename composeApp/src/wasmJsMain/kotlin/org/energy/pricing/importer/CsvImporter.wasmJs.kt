package org.energy.pricing.importer

import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

actual fun pickCsvFileContent(onResult: (String?) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".csv,text/csv"
    input.onchange = {
        val file = input.files?.item(0)
        if (file == null) {
            onResult(null)
        } else {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result?.toString()
                onResult(result)
            }
            reader.onerror = {
                onResult(null)
            }
            reader.readAsText(file)
        }
    }
    // Trigger the file dialog
    input.click()
}
