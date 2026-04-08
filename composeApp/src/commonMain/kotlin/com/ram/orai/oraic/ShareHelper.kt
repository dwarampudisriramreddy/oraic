package com.ram.orai.oraic

import java.io.File

/**
 * Platform-specific file sharing functionality
 */
expect class ShareHelper {
    fun shareFile(file: File, mimeType: String, fileName: String)
    fun sharePdfReport(patient: Patient, pdfBytes: ByteArray)
    fun shareCsvFile(csvContent: String, fileName: String)
}

expect fun createShareHelper(context: Any?): ShareHelper

