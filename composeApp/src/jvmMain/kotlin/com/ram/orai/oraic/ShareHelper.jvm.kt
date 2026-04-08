package com.ram.orai.oraic

import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

actual class ShareHelper {
    actual fun shareFile(file: File, mimeType: String, fileName: String) {
        // On Desktop, open file location or open with default application
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file.parentFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    actual fun sharePdfReport(patient: Patient, pdfBytes: ByteArray) {
        try {
            val fileName = generateReportFileName(patient)
            val downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads")
            if (!Files.exists(downloadsDir)) {
                Files.createDirectories(downloadsDir)
            }
            val file = File(downloadsDir.toFile(), fileName)
            FileOutputStream(file).use { it.write(pdfBytes) }
            
            // Open file with default PDF viewer
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    actual fun shareCsvFile(csvContent: String, fileName: String) {
        try {
            val downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads")
            if (!Files.exists(downloadsDir)) {
                Files.createDirectories(downloadsDir)
            }
            val file = File(downloadsDir.toFile(), fileName)
            FileOutputStream(file).use { 
                it.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            
            // Open file with default CSV viewer
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun createShareHelper(context: Any?): ShareHelper {
    return ShareHelper()
}


