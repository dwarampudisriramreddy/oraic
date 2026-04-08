package com.ram.orai.oraic

actual class PdfReportGenerator {
    actual fun generateReport(patient: Patient, svgContent: String): ByteArray? {
        // iOS implementation would use Core Graphics PDF APIs
        // For now, return null - can be implemented later
        return null
    }
}

actual fun createPdfReportGenerator(context: Any?): PdfReportGenerator {
    return PdfReportGenerator()
}




