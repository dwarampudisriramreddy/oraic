package com.ram.orai.oraic

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import com.caverock.androidsvg.SVG
import java.io.ByteArrayOutputStream

actual class PdfReportGenerator(private val context: Context) {
    // Professional color scheme
    private val primaryColor = Color.rgb(25, 118, 210) // Blue
    private val secondaryColor = Color.rgb(66, 165, 245) // Light Blue
    private val accentColor = Color.rgb(76, 175, 80) // Green
    private val textColor = Color.rgb(33, 33, 33) // Dark Gray
    private val lightGray = Color.rgb(245, 245, 245)
    private val borderGray = Color.rgb(200, 200, 200)
    
    actual fun generateReport(patient: Patient, svgContent: String): ByteArray? {
        return try {
            val document = PdfDocument()
            val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 size
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var pageNumber = 1
            
            var yPosition = drawHeader(canvas, pageInfo, pageNumber)
            yPosition = drawTitleSection(canvas, yPosition, pageInfo)
            yPosition = drawPatientInformation(canvas, patient, yPosition, pageInfo)
            
            // Check if new page needed
            if (yPosition > 700f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawHeader(canvas, pageInfo, pageNumber)
            }
            
            yPosition = drawClinicalInformation(canvas, patient, yPosition, pageInfo)
            
            // Check if new page needed
            if (yPosition > 700f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawHeader(canvas, pageInfo, pageNumber)
            }
            
            yPosition = drawAssessmentSummary(canvas, patient, yPosition, pageInfo)
            
            // Finish current page before odontogram
            document.finishPage(page)
            pageNumber++
            
            // Start new page dedicated to odontogram
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = drawHeader(canvas, pageInfo, pageNumber)
            
            // Center the odontogram vertically on the page
            // Calculate remaining page height after header
            val availableHeight = pageInfo.pageHeight - yPosition - 50f // 50f for bottom margin
            val odontogramHeight = 420f // Expected height of odontogram + legend
            val topMargin = (availableHeight - odontogramHeight) / 2f
            yPosition += topMargin
            
            yPosition = drawOdontogram(canvas, svgContent, yPosition, pageInfo)
            
            // Finish odontogram page and start new page for remaining content
            document.finishPage(page)
            pageNumber++
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = drawHeader(canvas, pageInfo, pageNumber)
            
            yPosition = drawTreatmentSummary(canvas, patient, yPosition, pageInfo)
            
            // Add more sections with page breaks as needed
            if (yPosition > 600f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawHeader(canvas, pageInfo, pageNumber)
            }
            
            yPosition = drawDevelopmentalAnomalies(canvas, patient, yPosition, pageInfo)
            yPosition = drawPathologicalConditions(canvas, patient, yPosition, pageInfo)
            yPosition = drawPulpVitality(canvas, patient, yPosition, pageInfo)
            yPosition = drawPeriodontalParameters(canvas, patient, yPosition, pageInfo)
            
            // Check if we need a new page for gingival segment table
            if (yPosition > 550f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawHeader(canvas, pageInfo, pageNumber)
            }
            
            yPosition = drawGingivalSegmentTable(canvas, patient, yPosition, pageInfo)
            yPosition = drawLesionCharacteristics(canvas, patient, yPosition, pageInfo)
            yPosition = drawToothStatusTable(canvas, patient, yPosition, pageInfo)
            
            // Final Diagnosis and Treatment Plan
            yPosition = drawFinalDiagnosis(canvas, patient, yPosition, pageInfo)
            yPosition = drawTreatmentPlan(canvas, patient, yPosition, pageInfo)
            
            drawFooter(canvas, pageInfo, pageNumber)
            document.finishPage(page)
            
            val outputStream = ByteArrayOutputStream()
            document.writeTo(outputStream)
            document.close()
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun drawHeader(canvas: Canvas, pageInfo: PageInfo, pageNumber: Int): Float {
        val paint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageInfo.pageWidth.toFloat(), 40f, paint)
        
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
        }
        canvas.drawText("Latha Multi-Speciality Dental Clinic - Amalapuram", 50f, 25f, textPaint)
        
        val pagePaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
        }
        val pageText = "Page $pageNumber"
        val pageTextWidth = pagePaint.measureText(pageText)
        canvas.drawText(pageText, pageInfo.pageWidth - pageTextWidth - 50f, 25f, pagePaint)
        
        return 50f
    }
    
    private fun drawTitleSection(canvas: Canvas, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        
        val paint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(50f, yPosition, pageInfo.pageWidth - 50f, yPosition + 90f, paint)
        
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Latha Multi-Speciality Dental Clinic", pageInfo.pageWidth / 2f, yPosition + 30f, titlePaint)
        
        val locationPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Amalapuram", pageInfo.pageWidth / 2f, yPosition + 50f, locationPaint)
        
        val subtitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Dental Case History Report", pageInfo.pageWidth / 2f, yPosition + 75f, subtitlePaint)
        
        titlePaint.textAlign = Paint.Align.LEFT
        return yPosition + 100f
    }
    
    private fun drawPatientInformation(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Patient Information", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        // Draw underline
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = textColor
            textSize = 11f
        }
        
        yPosition = drawInfoRow(canvas, "Patient ID:", patient.id, yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Name:", patient.displayName, yPosition, labelPaint, valuePaint)
        if (patient.phoneNumber.isNotEmpty()) {
            yPosition = drawInfoRow(canvas, "Phone:", patient.phoneNumber, yPosition, labelPaint, valuePaint)
        }
        if (patient.email.isNotEmpty()) {
            yPosition = drawInfoRow(canvas, "Email:", patient.email, yPosition, labelPaint, valuePaint)
        }
        if (patient.address.isNotEmpty()) {
            yPosition = drawInfoRow(canvas, "Address:", patient.address, yPosition, labelPaint, valuePaint)
        }
        yPosition = drawInfoRow(canvas, "Report Date:", formatDate(patient.updatedAt), yPosition, labelPaint, valuePaint)
        
        return yPosition + 20f
    }
    
    private fun drawClinicalInformation(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Clinical Information", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val info = patient.dentalState.patientInfo
        val labelPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = textColor
            textSize = 11f
        }
        
        if (info.dob.isNotEmpty()) {
            yPosition = drawInfoRow(canvas, "Date of Birth:", info.dob, yPosition, labelPaint, valuePaint)
        }
        yPosition = drawInfoRow(canvas, "Dentition:", info.dentition, yPosition, labelPaint, valuePaint)
        if (info.chiefComplaint.isNotEmpty()) {
            yPosition = drawInfoRow(canvas, "Chief Complaint:", info.chiefComplaint, yPosition, labelPaint, valuePaint)
            if (info.chiefComplaintRegion.isNotEmpty()) {
                yPosition = drawInfoRow(canvas, "Location:", info.chiefComplaintRegion, yPosition, labelPaint, valuePaint)
            }
            if (info.chiefComplaintSince.isNotEmpty()) {
                yPosition = drawInfoRow(canvas, "Duration:", info.chiefComplaintSince, yPosition, labelPaint, valuePaint)
            }
        }
        yPosition = drawInfoRow(canvas, "Smoking Status:", info.smokingStatus, yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Diabetic Status:", info.diabeticStatus, yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Oral Hygiene:", info.oralHygiene, yPosition, labelPaint, valuePaint)
        
        if (info.malocclusion.isNotEmpty() && info.malocclusion != "None") {
            yPosition = drawInfoRow(canvas, "Malocclusion:", info.malocclusion, yPosition, labelPaint, valuePaint)
        }
        if (info.oralMucosa.isNotEmpty() && info.oralMucosa != "No issues") {
            yPosition = drawInfoRow(canvas, "Oral Mucosa:", info.oralMucosa, yPosition, labelPaint, valuePaint)
        }
        
        return yPosition + 20f
    }
    
    private fun drawOdontogram(canvas: Canvas, svgContent: String, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Odontogram", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        // Check SVG content and show detailed error messages in PDF
        var conversionError: String? = null
        var lastConversionError: String? = null
        
        if (svgContent.isEmpty()) {
            conversionError = "SVG content is empty. File: oral.svg not found in assets folder."
        } else if (!svgContent.contains("<svg", ignoreCase = true)) {
            conversionError = "Invalid SVG format. Content doesn't contain <svg> tag. Length: ${svgContent.length}"
        } else {
            android.util.Log.d("PdfReportGenerator", "Converting SVG to bitmap, SVG length: ${svgContent.length}")
            // Use same dimensions as JVM version: 550x420, then scale to fit
            val odontogramBitmap = convertSvgToBitmapWithError(svgContent, 550, 420) { error ->
                lastConversionError = error
            }
            if (odontogramBitmap != null) {
                android.util.Log.d("PdfReportGenerator", "Bitmap created successfully: ${odontogramBitmap.width}x${odontogramBitmap.height}")
                
                // Get actual bitmap dimensions
                val bitmapWidth = odontogramBitmap.width.toFloat()
                val bitmapHeight = odontogramBitmap.height.toFloat()
                
                // Center horizontally on the page
                val pageWidth = pageInfo.pageWidth.toFloat()
                val xPos = (pageWidth - bitmapWidth) / 2f
                
                android.util.Log.d("PdfReportGenerator", "Drawing bitmap - Page width: $pageWidth, Bitmap width: $bitmapWidth, X position: $xPos")
                
                // Draw bitmap at actual size, centered
                val bitmapPaint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                }
                
                canvas.drawBitmap(
                    odontogramBitmap, 
                    null, 
                    RectF(xPos, yPosition, xPos + bitmapWidth, yPosition + bitmapHeight), 
                    bitmapPaint
                )
                yPosition += bitmapHeight + 15f  // Match JVM spacing
                
                // Draw legend centered relative to the bitmap
                yPosition = drawLegend(canvas, yPosition, pageInfo, bitmapWidth, xPos)
            } else {
                // Get detailed error information
                val svgStart = svgContent.take(150).replace("\n", " ").replace("\r", "").take(100)
                val hasSvgTag = svgContent.contains("<svg", ignoreCase = true)
                val hasXmlns = svgContent.contains("xmlns=")
                val hasXmlDecl = svgContent.startsWith("<?xml")
                val svgEnd = svgContent.takeLast(100).replace("\n", " ").replace("\r", "")
                
                conversionError = buildString {
                    append("Odontogram image not available in PDF.\n\n")
                    append("The odontogram can be viewed in the app but cannot be included in the PDF report on Android due to SVG rendering limitations.\n\n")
                    if (lastConversionError != null && !lastConversionError.contains("not available")) {
                        append("Technical details: $lastConversionError\n\n")
                    }
                    append("All other patient information is included in this report.")
                }
            }
        }
        
        // Draw error message if conversion failed
        if (conversionError != null) {
            val errorPaint = Paint().apply {
                color = Color.RED
                textSize = 9f
                style = Paint.Style.FILL
            }
            
            // Draw error message with word wrapping
            val maxWidth = pageInfo.pageWidth - 100f
            val lines = wrapText(conversionError, errorPaint, maxWidth)
            lines.forEach { line ->
                canvas.drawText(line, 50f, yPosition, errorPaint)
                yPosition += 15f
            }
            
            // Also show helpful instructions
            val helpPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 8f
                style = Paint.Style.FILL
            }
            yPosition += 10f
            val helpText = "Note: Ensure oral.svg exists in src/main/assets/oral.svg"
            canvas.drawText(helpText, 50f, yPosition, helpPaint)
            yPosition += 15f
        }
        
        return yPosition + 20f
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            
            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return if (lines.isEmpty()) listOf(text) else lines
    }
    
    private fun drawLegend(canvas: Canvas, yPos: Float, pageInfo: PageInfo, bitmapWidth: Float? = null, bitmapXPos: Float? = null): Float {
        var yPosition = yPos
        val legendPaint = Paint().apply {
            color = textColor
            textSize = 9f
        }
        
        val legends = listOf(
            Pair(Color.rgb(0, 255, 0), "Treated"),
            Pair(Color.rgb(128, 128, 128), "Not Examined"),
            Pair(Color.rgb(255, 0, 0), "Abnormal"),
            Pair(Color.rgb(0, 0, 0), "Missing")
        )
        
        // Center legend relative to bitmap if provided, otherwise center on page
        val legendTotalWidth = 400f // 4 items × 100px spacing
        val xPos = if (bitmapWidth != null && bitmapXPos != null) {
            // Center legend relative to bitmap
            bitmapXPos + (bitmapWidth - legendTotalWidth) / 2f
        } else {
            // Center legend on page
            (pageInfo.pageWidth - legendTotalWidth) / 2f
        }
        
        var currentXPos = xPos
        legends.forEach { (color, label) ->
            val boxPaint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
            }
            val borderPaint = Paint().apply {
                this.color = borderGray
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRect(currentXPos, yPosition, currentXPos + 15f, yPosition + 12f, boxPaint)
            canvas.drawRect(currentXPos, yPosition, currentXPos + 15f, yPosition + 12f, borderPaint)
            canvas.drawText(label, currentXPos + 20f, yPosition + 10f, legendPaint)
            currentXPos += 100f
        }
        
        return yPosition + 25f
    }
    
    private fun drawAssessmentSummary(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Assessment Summary", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val state = patient.dentalState
        
        // Filter teeth based on dentition type
        val permanentTeeth = listOf(11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 38, 41, 42, 43, 44, 45, 46, 47, 48)
        val primaryTeeth = listOf(51, 52, 53, 54, 55, 61, 62, 63, 64, 65, 71, 72, 73, 74, 75, 81, 82, 83, 84, 85)
        
        val relevantToothNumbers = when (state.patientInfo.dentition) {
            "Permanent" -> permanentTeeth
            "Primary" -> primaryTeeth
            "Mixed" -> permanentTeeth + primaryTeeth
            "Edentulous" -> emptyList()
            else -> permanentTeeth
        }
        
        val relevantTeeth = state.teeth.values.filter { it.number in relevantToothNumbers }
        
        val totalTeeth = relevantTeeth.size
        val normalTeeth = relevantTeeth.count { it.status.equals("normal", ignoreCase = true) }
        val abnormalTeeth = relevantTeeth.count { it.status.equals("abnormal", ignoreCase = true) }
        val missingTeeth = relevantTeeth.count { it.status.equals("missing", ignoreCase = true) }
        val treatedTeeth = relevantTeeth.count { it.status.equals("treated", ignoreCase = true) }
        val notExaminedTeeth = relevantTeeth.count { it.status.equals("not examined", ignoreCase = true) }
        
        val labelPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = textColor
            textSize = 11f
        }
        
        yPosition = drawInfoRow(canvas, "Total Teeth:", totalTeeth.toString(), yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Normal Teeth:", normalTeeth.toString(), yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Abnormal Teeth:", abnormalTeeth.toString(), yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Missing Teeth:", missingTeeth.toString(), yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Treated Teeth:", treatedTeeth.toString(), yPosition, labelPaint, valuePaint)
        yPosition = drawInfoRow(canvas, "Not Examined:", notExaminedTeeth.toString(), yPosition, labelPaint, valuePaint)
        
        return yPosition + 20f
    }
    
    private fun drawTreatmentSummary(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        val state = patient.dentalState
        val treatedTeeth = state.teeth.values.filter { it.status.equals("treated", ignoreCase = true) }
        
        if (treatedTeeth.isEmpty()) return yPosition
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Treatment Summary", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val treatmentTypes = listOf("restoration", "root canal", "implant", "prosthesis")
        val labelPaint = Paint().apply {
            color = textColor
            textSize = 11f
            isFakeBoldText = true
        }
        val valuePaint = Paint().apply {
            color = textColor
            textSize = 11f
        }
        
        treatmentTypes.forEach { treatmentType ->
            val teeth = treatedTeeth
                .filter { it.treatmentType.equals(treatmentType, ignoreCase = true) }
                .map { it.number }
                .sorted()
            
            if (teeth.isNotEmpty()) {
                val treatmentLabel = treatmentType.replaceFirstChar { it.uppercaseChar() }
                yPosition = drawInfoRow(canvas, "$treatmentLabel:", teeth.joinToString(", "), yPosition, labelPaint, valuePaint)
            }
        }
        
        return yPosition + 20f
    }
    
    private fun drawDevelopmentalAnomalies(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val state = patient.dentalState
        val anomalies = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.toothSize.contains("+") && tooth.toothSize.contains("Macrodontia")) anomalies.getOrPut("Macrodontia") { mutableListOf() }.add(tooth.number)
            if (tooth.toothSize.contains("-") && tooth.toothSize.contains("Microdontia")) anomalies.getOrPut("Microdontia") { mutableListOf() }.add(tooth.number)
            if (tooth.toothMorphology.contains("+") && !tooth.toothMorphology.contains("0 =")) anomalies.getOrPut(tooth.toothMorphology.substringAfter(" = ").substringBefore(" (")) { mutableListOf() }.add(tooth.number)
            if (tooth.eruptionStatus.contains("-") && (tooth.eruptionStatus.contains("Impacted") || tooth.eruptionStatus.contains("Embedded") || tooth.eruptionStatus.contains("Ectopic"))) anomalies.getOrPut(tooth.eruptionStatus.substringAfter(" = ").substringBefore(" (")) { mutableListOf() }.add(tooth.number)
        }
        
        if (anomalies.isEmpty()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Developmental Anomalies", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply { color = textColor; textSize = 11f; isFakeBoldText = true }
        val valuePaint = Paint().apply { color = textColor; textSize = 11f }
        
        anomalies.forEach { (label, teeth) ->
            yPosition = drawInfoRow(canvas, "$label:", teeth.sorted().joinToString(", "), yPosition, labelPaint, valuePaint)
        }
        
        return yPosition + 20f
    }
    
    private fun drawPathologicalConditions(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val state = patient.dentalState
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.caries.contains("ICDAS") && !tooth.caries.contains("ICDAS 0")) conditions.getOrPut("Caries") { mutableListOf() }.add(tooth.number)
            if (tooth.erosion.contains("+") && !tooth.erosion.contains("0 =")) conditions.getOrPut("Erosion") { mutableListOf() }.add(tooth.number)
            if (tooth.fractureType.contains("+") && !tooth.fractureType.contains("0 =")) conditions.getOrPut("Fracture") { mutableListOf() }.add(tooth.number)
            if (tooth.attrition.contains("+") && !tooth.attrition.contains("0 =")) conditions.getOrPut("Attrition") { mutableListOf() }.add(tooth.number)
            if (tooth.abrasion.contains("+") && !tooth.abrasion.contains("0 =")) conditions.getOrPut("Abrasion") { mutableListOf() }.add(tooth.number)
            if (tooth.discoloration.contains("+") && !tooth.discoloration.contains("0 =")) conditions.getOrPut("Discoloration") { mutableListOf() }.add(tooth.number)
        }
        
        if (conditions.isEmpty()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Pathological & Traumatic Conditions", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply { color = textColor; textSize = 11f; isFakeBoldText = true }
        val valuePaint = Paint().apply { color = textColor; textSize = 11f }
        
        conditions.forEach { (label, teeth) ->
            yPosition = drawInfoRow(canvas, "$label:", teeth.sorted().joinToString(", "), yPosition, labelPaint, valuePaint)
        }
        
        return yPosition + 20f
    }
    
    private fun drawPulpVitality(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val state = patient.dentalState
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.painStatus.contains("pulpitis") || tooth.painStatus.contains("necrotic")) conditions.getOrPut("Pain/Pulpitis") { mutableListOf() }.add(tooth.number)
            if (tooth.tenderOnPercussion.contains("+1")) conditions.getOrPut("Tender on Percussion") { mutableListOf() }.add(tooth.number)
            if (tooth.sinusTract.contains("+")) conditions.getOrPut("Sinus Tract") { mutableListOf() }.add(tooth.number)
        }
        
        if (conditions.isEmpty()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Pulp Vitality & Pain Conditions", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply { color = textColor; textSize = 11f; isFakeBoldText = true }
        val valuePaint = Paint().apply { color = textColor; textSize = 11f }
        
        conditions.forEach { (label, teeth) ->
            yPosition = drawInfoRow(canvas, "$label:", teeth.sorted().joinToString(", "), yPosition, labelPaint, valuePaint)
        }
        
        return yPosition + 20f
    }
    
    private fun drawPeriodontalParameters(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val state = patient.dentalState
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.mobility != "None") conditions.getOrPut("Mobility - ${tooth.mobility}") { mutableListOf() }.add(tooth.number)
            if (!tooth.pocket.contains("Normal")) conditions.getOrPut("Pocket - ${tooth.pocket}") { mutableListOf() }.add(tooth.number)
            if (tooth.recession != "None") conditions.getOrPut("Recession - ${tooth.recession}") { mutableListOf() }.add(tooth.number)
            if (tooth.furcation != "None") conditions.getOrPut("Furcation - ${tooth.furcation}") { mutableListOf() }.add(tooth.number)
        }
        
        if (conditions.isEmpty()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Periodontal Parameters", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply { color = textColor; textSize = 11f; isFakeBoldText = true }
        val valuePaint = Paint().apply { color = textColor; textSize = 11f }
        
        conditions.forEach { (label, teeth) ->
            yPosition = drawInfoRow(canvas, "$label:", teeth.sorted().joinToString(", "), yPosition, labelPaint, valuePaint)
        }
        
        return yPosition + 20f
    }
    
    private fun drawGingivalSegmentTable(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val segments = patient.dentalState.gingivalSegments
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Gingival Segment Assessment", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 25f
        
        // Table parameters
        val cellWidth = (pageInfo.pageWidth - 100f) / 7f // 7 columns
        val cellHeight = 30f
        val headerPaint = Paint().apply { 
            color = android.graphics.Color.WHITE
            textSize = 9f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val cellPaint = Paint().apply { 
            color = textColor
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        val labelPaint = Paint().apply { 
            color = textColor
            textSize = 8f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        val borderPaint = Paint().apply { 
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerBgPaint = Paint().apply { color = primaryColor; style = Paint.Style.FILL }
        val evenRowPaint = Paint().apply { color = android.graphics.Color.parseColor("#F5F5F5"); style = Paint.Style.FILL }
        
        // Draw header row
        var xPosition = 50f
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 2.5f, yPosition + cellHeight, headerBgPaint)
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 2.5f, yPosition + cellHeight, borderPaint)
        canvas.drawText("Parameter", xPosition + (cellWidth * 2.5f) / 2, yPosition + cellHeight / 2 + 5f, headerPaint)
        xPosition += cellWidth * 2.5f
        
        for (segNum in 1..6) {
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 0.75f, yPosition + cellHeight, headerBgPaint)
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 0.75f, yPosition + cellHeight, borderPaint)
            canvas.drawText("S$segNum", xPosition + (cellWidth * 0.75f) / 2, yPosition + cellHeight / 2 + 5f, headerPaint)
            xPosition += cellWidth * 0.75f
        }
        yPosition += cellHeight
        
        // Parameters
        val parameters = listOf(
            "Gingival Color" to { seg: GingivalSegment -> seg.color },
            "Gingival Size" to { seg: GingivalSegment -> seg.sizeEdema },
            "Shape" to { seg: GingivalSegment -> seg.shape },
            "Contour" to { seg: GingivalSegment -> seg.contour },
            "Texture" to { seg: GingivalSegment -> seg.texture },
            "Consistency" to { seg: GingivalSegment -> seg.consistency },
            "Distribution" to { seg: GingivalSegment -> seg.distribution },
            "Bleeding" to { seg: GingivalSegment -> seg.bleeding },
            "Exudate" to { seg: GingivalSegment -> seg.exudate },
            "Calculus" to { seg: GingivalSegment -> seg.calculus },
            "Plaque" to { seg: GingivalSegment -> seg.plaque }
        )
        
        // Draw data rows
        parameters.forEachIndexed { index, (paramName, getter) ->
            xPosition = 50f
            
            // Draw background for even rows
            if (index % 2 == 0) {
                canvas.drawRect(xPosition, yPosition, pageInfo.pageWidth - 50f, yPosition + cellHeight, evenRowPaint)
            }
            
            // Parameter label
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 2.5f, yPosition + cellHeight, borderPaint)
            canvas.drawText(paramName, xPosition + 5f, yPosition + cellHeight / 2 + 5f, labelPaint)
            xPosition += cellWidth * 2.5f
            
            // Segment values
            for (segNum in 1..6) {
                val segment = segments[segNum]
                val value = segment?.let { getter(it) } ?: "-"
                // Truncate long values
                val displayValue = if (value.length > 12) value.substring(0, 10) + ".." else value
                
                canvas.drawRect(xPosition, yPosition, xPosition + cellWidth * 0.75f, yPosition + cellHeight, borderPaint)
                canvas.drawText(displayValue, xPosition + (cellWidth * 0.75f) / 2, yPosition + cellHeight / 2 + 5f, cellPaint)
                xPosition += cellWidth * 0.75f
            }
            
            yPosition += cellHeight
        }
        
        return yPosition + 20f
    }
    
    private fun drawLesionCharacteristics(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val info = patient.dentalState.patientInfo
        if (info.oralMucosa == "No issues" || info.lesionType.contains("Normal mucosa")) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Lesion Characteristics", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val labelPaint = Paint().apply { color = textColor; textSize = 11f; isFakeBoldText = true }
        val valuePaint = Paint().apply { color = textColor; textSize = 11f }
        
        if (!info.lesionType.contains("Normal mucosa")) yPosition = drawInfoRow(canvas, "Type:", info.lesionType, yPosition, labelPaint, valuePaint)
        if (!info.lesionSize.contains("0 =")) yPosition = drawInfoRow(canvas, "Size:", info.lesionSize, yPosition, labelPaint, valuePaint)
        if (!info.lesionColor.contains("Normal")) yPosition = drawInfoRow(canvas, "Color:", info.lesionColor, yPosition, labelPaint, valuePaint)
        if (!info.lesionPain.contains("No pain")) yPosition = drawInfoRow(canvas, "Pain:", info.lesionPain, yPosition, labelPaint, valuePaint)
        
        return yPosition + 20f
    }
    
    private fun drawToothStatusTable(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        var yPosition = yPos
        val state = patient.dentalState
        val abnormalTeeth = state.teeth.values.filter { 
            (it.status != "normal" && it.status != "not examined") ||
            (it.caries.contains("ICDAS") && !it.caries.contains("ICDAS 0"))
        }
        
        if (abnormalTeeth.isEmpty()) return yPosition
        
        val headingPaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Detailed Tooth Status", 50f, yPosition, headingPaint)
        yPosition += 25f
        
        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 2f
        }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        // Table header
        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            isFakeBoldText = true
        }
        val headerBgPaint = Paint().apply {
            color = primaryColor
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = borderGray
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        
        val colWidths = floatArrayOf(80f, 120f, 150f, 200f)
        val startX = 50f
        val rowHeight = 20f
        
        // Draw header background
        canvas.drawRect(startX, yPosition, startX + colWidths.sum(), yPosition + rowHeight, headerBgPaint)
        
        var xPos = startX
        val headers = listOf("Tooth", "Status", "Periodontal", "Notes")
        headers.forEachIndexed { index, header ->
            canvas.drawRect(xPos, yPosition, xPos + colWidths[index], yPosition + rowHeight, borderPaint)
            canvas.drawText(header, xPos + 5f, yPosition + 15f, headerPaint)
            xPos += colWidths[index]
        }
        yPosition += rowHeight
        
        // Draw data rows
        val dataPaint = Paint().apply {
            color = textColor
            textSize = 9f
        }
        val altBgPaint = Paint().apply {
            color = lightGray
            style = Paint.Style.FILL
        }
        
        abnormalTeeth.sortedBy { it.number }.forEachIndexed { index, tooth ->
            val isAltRow = index % 2 == 1
            if (isAltRow) {
                canvas.drawRect(startX, yPosition, startX + colWidths.sum(), yPosition + rowHeight, altBgPaint)
            }
            
            xPos = startX
            canvas.drawRect(xPos, yPosition, xPos + colWidths[0], yPosition + rowHeight, borderPaint)
            canvas.drawText(tooth.number.toString(), xPos + 5f, yPosition + 15f, dataPaint)
            xPos += colWidths[0]
            
            canvas.drawRect(xPos, yPosition, xPos + colWidths[1], yPosition + rowHeight, borderPaint)
            canvas.drawText(tooth.status, xPos + 5f, yPosition + 15f, dataPaint)
            xPos += colWidths[1]
            
            canvas.drawRect(xPos, yPosition, xPos + colWidths[2], yPosition + rowHeight, borderPaint)
            canvas.drawText(tooth.periodontalStatus, xPos + 5f, yPosition + 15f, dataPaint)
            xPos += colWidths[2]
            
            val notes = mutableListOf<String>()
            if (tooth.caries.contains("ICDAS") && !tooth.caries.contains("ICDAS 0")) {
                notes.add(tooth.caries.substringBefore(" - "))
            }
            if (tooth.treatmentType.isNotEmpty() && !tooth.status.equals("missing", ignoreCase = true)) {
                notes.add(tooth.treatmentType)
            }
            
            canvas.drawRect(xPos, yPosition, xPos + colWidths[3], yPosition + rowHeight, borderPaint)
            canvas.drawText(notes.joinToString("; "), xPos + 5f, yPosition + 15f, dataPaint)
            
            yPosition += rowHeight
        }
        
        return yPosition + 20f
    }
    
    private fun drawInfoRow(canvas: Canvas, label: String, value: String, yPos: Float, labelPaint: Paint, valuePaint: Paint): Float {
        val labelWidth = labelPaint.measureText(label)
        canvas.drawText(label, 50f, yPos, labelPaint)
        canvas.drawText(value, 50f + labelWidth + 10f, yPos, valuePaint)
        return yPos + 18f
    }
    
    private fun drawFooter(canvas: Canvas, pageInfo: PageInfo, pageNumber: Int) {
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
        }
        val footerText = "Page $pageNumber"
        val textWidth = footerPaint.measureText(footerText)
        canvas.drawText(footerText, (pageInfo.pageWidth - textWidth) / 2f, pageInfo.pageHeight - 20f, footerPaint)
    }
    
    private fun convertSvgToBitmapWithError(svgContent: String, width: Int, height: Int, onError: (String) -> Unit): Bitmap? {
        return convertSvgToBitmap(svgContent, width, height, onError)
    }
    
    private fun convertSvgToBitmap(svgContent: String, width: Int, height: Int, onError: ((String) -> Unit)? = null): Bitmap? {
        if (svgContent.isEmpty()) {
            return null
        }
        
        if (!svgContent.contains("<svg", ignoreCase = true)) {
            return null
        }
        
        // Use AndroidSVG - lightweight library specifically for Android
        return try {
            convertSvgWithAndroidSVG(svgContent, width, height, onError)
        } catch (e: Exception) {
            android.util.Log.e("PdfReportGenerator", "AndroidSVG conversion failed: ${e.message}", e)
            onError?.invoke("SVG rendering failed: ${e.message}")
            null
        }
    }
    
    private fun convertSvgWithAndroidSVG(svgContent: String, width: Int, height: Int, onError: ((String) -> Unit)?): Bitmap? {
        return try {
            // Process SVG - ensure proper format
            var processedSvg = svgContent.trim()
            
            // Remove XML declaration if present (AndroidSVG handles it internally)
            processedSvg = processedSvg.replaceFirst(Regex("""<\?xml[^>]*\?>[\s\n]*""", RegexOption.IGNORE_CASE), "")
            
            // Remove DOCTYPE declarations that can cause issues
            processedSvg = processedSvg.replace(Regex("""<!DOCTYPE[^>]*>[\s\n]*""", RegexOption.IGNORE_CASE), "")
            
            // Ensure SVG namespace is present
            if (!processedSvg.contains("xmlns=")) {
                processedSvg = processedSvg.replaceFirst(
                    "<svg",
                    "<svg xmlns=\"http://www.w3.org/2000/svg\""
                )
            }
            
            // Parse SVG using AndroidSVG
            val svg = SVG.getFromString(processedSvg)
            
            // Create bitmap at exact target dimensions
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            
            // Set the SVG to render into the specified bounds
            // This tells AndroidSVG to scale the entire SVG to fit the canvas
            svg.renderToCanvas(canvas, RectF(0f, 0f, width.toFloat(), height.toFloat()))
            
            android.util.Log.d("PdfReportGenerator", "Successfully converted SVG to bitmap using AndroidSVG (${width}x${height})")
            bitmap
        } catch (e: Exception) {
            android.util.Log.e("PdfReportGenerator", "AndroidSVG error: ${e.message}", e)
            onError?.invoke("AndroidSVG rendering failed: ${e.message}")
            null
        }
    }
    
    private fun drawFinalDiagnosis(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val finalDiagnosis = patient.dentalState.patientInfo.finalDiagnosis
        
        if (finalDiagnosis.isBlank()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Final Diagnosis", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val contentPaint = Paint().apply { color = textColor; textSize = 11f }
        val textBounds = Rect()
        contentPaint.getTextBounds(finalDiagnosis, 0, finalDiagnosis.length, textBounds)
        
        // Wrap text if needed
        val maxWidth = pageInfo.pageWidth - 100f
        val lines = breakTextIntoLines(finalDiagnosis, contentPaint, maxWidth)
        
        lines.forEach { line ->
            canvas.drawText(line, 50f, yPosition, contentPaint)
            yPosition += 18f
        }
        
        return yPosition + 20f
    }
    
    private fun drawTreatmentPlan(canvas: Canvas, patient: Patient, yPos: Float, pageInfo: PageInfo): Float {
        val treatmentPlans = patient.dentalState.patientInfo.treatmentPlans
            .filter { it.cdtCode.isNotEmpty() || it.description.isNotEmpty() }
        
        if (treatmentPlans.isEmpty()) return yPos
        
        var yPosition = yPos
        val headingPaint = Paint().apply { color = primaryColor; textSize = 16f; isFakeBoldText = true }
        canvas.drawText("Treatment Plan", 50f, yPosition, headingPaint)
        yPosition += 25f
        val linePaint = Paint().apply { color = primaryColor; strokeWidth = 2f }
        canvas.drawLine(50f, yPosition, pageInfo.pageWidth - 50f, yPosition, linePaint)
        yPosition += 20f
        
        val cellWidth = (pageInfo.pageWidth - 100f) / 3f
        val cellHeight = 30f
        val headerPaint = Paint().apply { 
            color = android.graphics.Color.WHITE
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val cellPaint = Paint().apply { 
            color = textColor
            textSize = 9f
            textAlign = Paint.Align.LEFT
        }
        val borderPaint = Paint().apply { 
            color = borderGray
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerBgPaint = Paint().apply { color = primaryColor; style = Paint.Style.FILL }
        val evenRowPaint = Paint().apply { color = lightGray; style = Paint.Style.FILL }
        
        // Draw header
        var xPosition = 50f
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, headerBgPaint)
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
        canvas.drawText("CDT Code", xPosition + cellWidth / 2, yPosition + cellHeight / 2 + 5f, headerPaint)
        xPosition += cellWidth
        
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, headerBgPaint)
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
        canvas.drawText("Description", xPosition + cellWidth / 2, yPosition + cellHeight / 2 + 5f, headerPaint)
        xPosition += cellWidth
        
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, headerBgPaint)
        canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
        canvas.drawText("Date", xPosition + cellWidth / 2, yPosition + cellHeight / 2 + 5f, headerPaint)
        yPosition += cellHeight
        
        // Draw data rows
        treatmentPlans.forEachIndexed { index, plan ->
            xPosition = 50f
            
            if (index % 2 == 0) {
                canvas.drawRect(xPosition, yPosition, pageInfo.pageWidth - 50f, yPosition + cellHeight, evenRowPaint)
            }
            
            val code = if (plan.cdtCode.isNotEmpty() && plan.cdtName.isNotEmpty()) {
                "${plan.cdtCode} - ${plan.cdtName}"
            } else if (plan.cdtCode.isNotEmpty()) {
                plan.cdtCode
            } else {
                "-"
            }
            
            cellPaint.textAlign = Paint.Align.CENTER
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
            canvas.drawText(code, xPosition + cellWidth / 2, yPosition + cellHeight / 2 + 5f, cellPaint)
            xPosition += cellWidth
            
            cellPaint.textAlign = Paint.Align.LEFT
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
            val descLines = breakTextIntoLines(plan.description.ifEmpty { "-" }, cellPaint, cellWidth - 10f)
            var descY = yPosition + 12f
            descLines.forEach { line ->
                canvas.drawText(line, xPosition + 5f, descY, cellPaint)
                descY += 12f
            }
            xPosition += cellWidth
            
            val timestampText = if (plan.timestamp.isNotEmpty()) {
                try {
                    val timestampLong = plan.timestamp.toLongOrNull()
                    if (timestampLong != null && timestampLong > 0) {
                        formatDate(timestampLong)
                    } else {
                        plan.timestamp
                    }
                } catch (e: Exception) {
                    plan.timestamp
                }
            } else {
                "-"
            }
            cellPaint.textAlign = Paint.Align.CENTER
            canvas.drawRect(xPosition, yPosition, xPosition + cellWidth, yPosition + cellHeight, borderPaint)
            canvas.drawText(timestampText, xPosition + cellWidth / 2, yPosition + cellHeight / 2 + 5f, cellPaint)
            
            yPosition += maxOf(cellHeight, descLines.size * 12f)
        }
        
        return yPosition + 20f
    }
    
    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            
            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines.ifEmpty { listOf(text) }
    }
}

actual fun createPdfReportGenerator(context: Any?): PdfReportGenerator {
    return PdfReportGenerator(context as Context)
}
