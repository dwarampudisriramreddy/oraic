package com.ram.orai.oraic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

expect fun getSvgContent(context: Any?): String

@Composable
fun rememberReportSharingHelper(
    pdfGenerator: PdfReportGenerator,
    shareHelper: ShareHelper,
    context: Any?
): ReportSharingHelper {
    return remember(pdfGenerator, shareHelper, context) { 
        ReportSharingHelper(pdfGenerator, shareHelper, context) 
    }
}

class ReportSharingHelper(
    private val pdfGenerator: PdfReportGenerator,
    private val shareHelper: ShareHelper,
    private val context: Any?
) {
    fun sharePatientReport(patient: Patient) {
        // Load and modify SVG
        val baseSvg = getSvgContent(context)
        if (baseSvg.isEmpty()) {
            // Still generate PDF without odontogram
            val pdfBytes = pdfGenerator.generateReport(patient, "")
            if (pdfBytes != null) {
                shareHelper.sharePdfReport(patient, pdfBytes)
            }
            return
        }
        
        val modifiedSvg = modifySvgXml(baseSvg, patient.dentalState)
        
        // Generate PDF
        val pdfBytes = pdfGenerator.generateReport(patient, modifiedSvg)
        
        if (pdfBytes != null) {
            shareHelper.sharePdfReport(patient, pdfBytes)
        }
    }
}

