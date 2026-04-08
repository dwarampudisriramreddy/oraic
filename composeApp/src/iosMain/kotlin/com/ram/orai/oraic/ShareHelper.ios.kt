package com.ram.orai.oraic

import java.io.File

actual class ShareHelper {
    actual fun shareFile(file: File, mimeType: String, fileName: String) {
        // iOS implementation would use UIActivityViewController
        // For now, do nothing - can be implemented later
    }
    
    actual fun sharePdfReport(patient: Patient, pdfBytes: ByteArray) {
        // iOS implementation
    }
    
    actual fun shareCsvFile(csvContent: String, fileName: String) {
        // iOS implementation - would use UIActivityViewController
    }
}

actual fun createShareHelper(context: Any?): ShareHelper {
    return ShareHelper()
}


