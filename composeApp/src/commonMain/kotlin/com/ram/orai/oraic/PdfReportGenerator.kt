package com.ram.orai.oraic

import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates a PDF report for a patient's case history including odontogram
 */
expect class PdfReportGenerator {
    fun generateReport(patient: Patient, svgContent: String): ByteArray?
}

expect fun createPdfReportGenerator(context: Any?): PdfReportGenerator

fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}

fun generateReportFileName(patient: Patient): String {
    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(Date(patient.updatedAt))
    val name = patient.displayName.replace(" ", "_").take(20)
    return "CaseHistory_${name}_$dateStr.pdf"
}

