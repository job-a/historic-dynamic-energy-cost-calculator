package org.energy.pricing.importer

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual fun pickCsvFileContent(onResult: (String?) -> Unit) {
    try {
        val chooser = JFileChooser().apply {
            fileFilter = FileNameExtensionFilter("CSV Files", "csv")
            isAcceptAllFileFilterUsed = true
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file: File = chooser.selectedFile
            val text = file.readText()
            onResult(text)
        } else {
            onResult(null)
        }
    } catch (t: Throwable) {
        t.printStackTrace()
        onResult(null)
    }
}
