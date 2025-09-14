package org.energy.pricing.io

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

actual fun pickCsvFileContent(onResult: (String?) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".csv,text/csv"
    // Hide and attach to DOM to improve reliability across browsers/environments
    (input as HTMLElement).style.display = "none"
    document.body?.appendChild(input)

    input.onchange = {
        val file = input.files?.item(0)
        fun cleanup() {
            // Remove from DOM to avoid leaks and allow re-creation next time
            kotlin.runCatching { document.body?.removeChild(input) }
        }
        if (file == null) {
            cleanup()
            onResult(null)
        } else {
            val reader = FileReader()
            reader.onload = {
                val result = reader.result?.toString()
                cleanup()
                onResult(result)
                null
            }
            reader.onerror = {
                cleanup()
                onResult(null)
                null
            }
            reader.readAsText(file)
        }
        null
    }
    // Reset value so selecting the same file again triggers change
    input.value = ""
    // Trigger the file dialog
    input.click()
}
