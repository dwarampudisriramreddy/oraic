package com.ram.orai.oraic

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.ByteArrayOutputStream
import java.io.StringReader

actual class PdfReportGenerator {
    // Professional color scheme
    private val primaryColor = BaseColor(25, 118, 210) // Blue
    private val secondaryColor = BaseColor(66, 165, 245) // Light Blue
    private val accentColor = BaseColor(76, 175, 80) // Green
    private val textColor = BaseColor(33, 33, 33) // Dark Gray
    private val lightGray = BaseColor(245, 245, 245)
    
    private fun formatDate(timestamp: Long): String {
        return try {
            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
    
    actual fun generateReport(patient: Patient, svgContent: String): ByteArray? {
        return try {
            val document = Document(PageSize.A4, 36f, 36f, 60f, 36f)
            val outputStream = ByteArrayOutputStream()
            val writer = PdfWriter.getInstance(document, outputStream)
            
            // Add header and footer
            writer.pageEvent = object : PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    addHeader(writer, document)
                    addFooter(writer, document)
                }
            }
            
            document.open()
            
            // Title Section with colored background
            addTitleSection(document, patient)
            
            // Patient Information in a professional table
            addPatientInformationSection(document, patient)
            
            // Clinical Information
            addClinicalInformationSection(document, patient)
            
            // Assessment Summary
            addAssessmentSummarySection(document, patient)
            
            // Odontogram
            addOdontogramSection(document, svgContent)
            
            // Treatment Summary
            addTreatmentSummarySection(document, patient)
            
            // Developmental Anomalies
            addDevelopmentalAnomaliesSection(document, patient)
            
            // Pathological & Traumatic Conditions
            addPathologicalConditionsSection(document, patient)
            
            // Pulp Vitality & Pain Conditions
            addPulpVitalitySection(document, patient)
            
            // Periodontal Parameters
            addPeriodontalParametersSection(document, patient)
            
            // Gingival Segment Assessment Matrix
            addGingivalSegmentTable(document, patient)
            
            // Lesion Characteristics (if oral mucosa issues exist)
            addLesionCharacteristicsSection(document, patient)
            
            // Tooth Status Details
            addToothStatusSection(document, patient)
            
            // Final Diagnosis and Treatment Plan
            addFinalDiagnosisSection(document, patient)
            addTreatmentPlanSection(document, patient)
            
            document.close()
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun addTitleSection(document: Document, patient: Patient) {
        // Colored title box
        val titleTable = PdfPTable(1)
        titleTable.widthPercentage = 100f
        titleTable.setWidths(floatArrayOf(1f))
        
        val titleCell = PdfPCell(Phrase("Latha Multi-Speciality Dental Clinic", Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, BaseColor.WHITE)))
        titleCell.backgroundColor = primaryColor
        titleCell.setPadding(15f)
        titleCell.horizontalAlignment = Element.ALIGN_CENTER
        titleCell.border = Rectangle.NO_BORDER
        titleTable.addCell(titleCell)
        
        val locationCell = PdfPCell(Phrase("Amalapuram", Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL, BaseColor.WHITE)))
        locationCell.backgroundColor = primaryColor
        locationCell.setPadding(5f)
        locationCell.setPaddingTop(0f)
        locationCell.horizontalAlignment = Element.ALIGN_CENTER
        locationCell.border = Rectangle.NO_BORDER
        titleTable.addCell(locationCell)
        
        val subtitleCell = PdfPCell(Phrase("Dental Case History Report", Font(Font.FontFamily.HELVETICA, 18f, Font.NORMAL, BaseColor.WHITE)))
        subtitleCell.backgroundColor = primaryColor
        subtitleCell.setPadding(10f)
        subtitleCell.setPaddingTop(0f)
        subtitleCell.horizontalAlignment = Element.ALIGN_CENTER
        subtitleCell.border = Rectangle.NO_BORDER
        titleTable.addCell(subtitleCell)
        
        document.add(titleTable)
        document.add(Paragraph(" ", Font(Font.FontFamily.HELVETICA, 12f)))
    }
    
    private fun addPatientInformationSection(document: Document, patient: Patient) {
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Patient Information", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(30f, 70f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        addTableRow(table, "Patient ID:", patient.id, labelFont, valueFont)
        addTableRow(table, "Name:", patient.displayName, labelFont, valueFont)
        if (patient.phoneNumber.isNotEmpty()) {
            addTableRow(table, "Phone:", patient.phoneNumber, labelFont, valueFont)
        }
        if (patient.email.isNotEmpty()) {
            addTableRow(table, "Email:", patient.email, labelFont, valueFont)
        }
        if (patient.address.isNotEmpty()) {
            addTableRow(table, "Address:", patient.address, labelFont, valueFont)
        }
        addTableRow(table, "Report Date:", formatDate(patient.updatedAt), labelFont, valueFont)
        
        document.add(table)
    }
    
    private fun addClinicalInformationSection(document: Document, patient: Patient) {
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Clinical Information", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val info = patient.dentalState.patientInfo
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(30f, 70f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        if (info.dob.isNotEmpty()) {
            addTableRow(table, "Date of Birth:", info.dob, labelFont, valueFont)
        }
        addTableRow(table, "Dentition:", info.dentition, labelFont, valueFont)
        if (info.chiefComplaint.isNotEmpty()) {
            addTableRow(table, "Chief Complaint:", info.chiefComplaint, labelFont, valueFont)
            if (info.chiefComplaintRegion.isNotEmpty()) {
                addTableRow(table, "Location:", info.chiefComplaintRegion, labelFont, valueFont)
            }
            if (info.chiefComplaintSince.isNotEmpty()) {
                addTableRow(table, "Duration:", info.chiefComplaintSince, labelFont, valueFont)
            }
        }
        addTableRow(table, "Smoking Status:", info.smokingStatus, labelFont, valueFont)
        addTableRow(table, "Diabetic Status:", info.diabeticStatus, labelFont, valueFont)
        addTableRow(table, "Oral Hygiene:", info.oralHygiene, labelFont, valueFont)
        
        if (info.malocclusion.isNotEmpty() && info.malocclusion != "None") {
            addTableRow(table, "Malocclusion:", info.malocclusion, labelFont, valueFont)
        }
        if (info.oralMucosa.isNotEmpty() && info.oralMucosa != "No issues") {
            addTableRow(table, "Oral Mucosa:", info.oralMucosa, labelFont, valueFont)
        }
        
        document.add(table)
    }
    
    private fun addOdontogramSection(document: Document, svgContent: String) {
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Odontogram", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val odontogramImage = convertSvgToImage(svgContent, 550, 420)
        if (odontogramImage != null) {
            val image = Image.getInstance(odontogramImage)
            image.scaleToFit(520f, 400f)
            image.alignment = Element.ALIGN_CENTER
            image.spacingAfter = 15f
            document.add(image)
            
            // Add legend
            addLegend(document)
        } else {
            val errorFont = Font(Font.FontFamily.HELVETICA, 10f, Font.ITALIC, BaseColor.RED)
            document.add(Paragraph("Odontogram image could not be generated", errorFont))
        }
    }
    
    private fun addLegend(document: Document) {
        val legendTable = PdfPTable(4)
        legendTable.widthPercentage = 80f
        legendTable.horizontalAlignment = Element.ALIGN_CENTER
        legendTable.spacingBefore = 5f
        legendTable.spacingAfter = 15f
        
        val legendFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, textColor)
        
        fun addLegendItem(color: BaseColor, label: String) {
            val cell = PdfPCell()
            cell.border = Rectangle.NO_BORDER
            cell.setPadding(5f)
            
            val colorBox = PdfPCell()
            colorBox.backgroundColor = color
            colorBox.fixedHeight = 12f
            colorBox.border = Rectangle.BOX
            colorBox.borderColor = BaseColor.GRAY
            colorBox.borderWidth = 0.5f
            
            val labelCell = PdfPCell(Phrase(label, legendFont))
            labelCell.border = Rectangle.NO_BORDER
            labelCell.paddingLeft = 5f
            
            val container = PdfPTable(2)
            container.setWidths(floatArrayOf(15f, 85f))
            container.addCell(colorBox)
            container.addCell(labelCell)
            
            cell.addElement(container)
            legendTable.addCell(cell)
        }
        
        addLegendItem(BaseColor(0, 255, 0), "Treated")
        addLegendItem(BaseColor(128, 128, 128), "Not Examined")
        addLegendItem(BaseColor(255, 0, 0), "Abnormal")
        addLegendItem(BaseColor(0, 0, 0), "Missing")
        
        document.add(legendTable)
    }
    
    private fun addAssessmentSummarySection(document: Document, patient: Patient) {
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Assessment Summary", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val state = patient.dentalState
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(50f, 50f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
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
        
        addTableRow(table, "Total Teeth:", totalTeeth.toString(), labelFont, valueFont)
        addTableRow(table, "Normal Teeth:", normalTeeth.toString(), labelFont, valueFont)
        addTableRow(table, "Abnormal Teeth:", abnormalTeeth.toString(), labelFont, valueFont)
        addTableRow(table, "Missing Teeth:", missingTeeth.toString(), labelFont, valueFont)
        addTableRow(table, "Treated Teeth:", treatedTeeth.toString(), labelFont, valueFont)
        addTableRow(table, "Not Examined:", notExaminedTeeth.toString(), labelFont, valueFont)
        
        document.add(table)
    }
    
    private fun addTreatmentSummarySection(document: Document, patient: Patient) {
        val state = patient.dentalState
        val treatedTeeth = state.teeth.values.filter { it.status.equals("treated", ignoreCase = true) }
        
        if (treatedTeeth.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Treatment Summary", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val treatmentTypes = listOf("restoration", "root canal", "implant", "prosthesis")
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(40f, 60f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        treatmentTypes.forEach { treatmentType ->
            val teeth = treatedTeeth
                .filter { it.treatmentType.equals(treatmentType, ignoreCase = true) }
                .map { it.number }
                .sorted()
            
            if (teeth.isNotEmpty()) {
                val treatmentLabel = treatmentType.replaceFirstChar { it.uppercaseChar() }
                addTableRow(table, "$treatmentLabel:", teeth.joinToString(", "), labelFont, valueFont)
            }
        }
        
        val otherTeeth = treatedTeeth
            .filter { it.treatmentType.isEmpty() || !treatmentTypes.any { type -> it.treatmentType.equals(type, ignoreCase = true) } }
            .map { it.number }
            .sorted()
        
        if (otherTeeth.isNotEmpty()) {
            addTableRow(table, "Other Treatments:", otherTeeth.joinToString(", "), labelFont, valueFont)
        }
        
        if (table.rows.size > 0) {
            document.add(table)
        }
    }
    
    private fun addDevelopmentalAnomaliesSection(document: Document, patient: Patient) {
        val state = patient.dentalState
        
        // Collect all developmental anomalies
        val anomalies = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.toothSize.contains("+") && tooth.toothSize.contains("Macrodontia")) {
                anomalies.getOrPut("Macrodontia") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothSize.contains("-") && tooth.toothSize.contains("Microdontia")) {
                anomalies.getOrPut("Microdontia") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothShape.contains("+2") && tooth.toothShape.contains("Twinning")) {
                anomalies.getOrPut("Twinning") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothShape.contains("+1") && tooth.toothShape.contains("Gemination")) {
                anomalies.getOrPut("Gemination") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothShape.contains("-1") && tooth.toothShape.contains("Fusion")) {
                anomalies.getOrPut("Fusion") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothShape.contains("-2") && tooth.toothShape.contains("Concrescence")) {
                anomalies.getOrPut("Concrescence") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothMorphology.contains("+4") && tooth.toothMorphology.contains("Talon cusp")) {
                anomalies.getOrPut("Talon Cusp") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothMorphology.contains("+2") && tooth.toothMorphology.contains("Taurodontism")) {
                anomalies.getOrPut("Taurodontism") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothMorphology.contains("+1") && tooth.toothMorphology.contains("Dens evaginatus")) {
                anomalies.getOrPut("Dens Evaginatus") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothMorphology.contains("-") && tooth.toothMorphology.contains("Dens invaginatus")) {
                anomalies.getOrPut("Dens Invaginatus") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.toothMorphology.contains("-4") && tooth.toothMorphology.contains("Dilaceration")) {
                anomalies.getOrPut("Dilaceration") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.eruptionStatus.contains("-4") && tooth.eruptionStatus.contains("Impacted")) {
                anomalies.getOrPut("Impacted") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.eruptionStatus.contains("-3") && tooth.eruptionStatus.contains("Embedded")) {
                anomalies.getOrPut("Embedded") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.eruptionStatus.contains("-5") && tooth.eruptionStatus.contains("Ectopic")) {
                anomalies.getOrPut("Ectopic Eruption") { mutableListOf() }.add(tooth.number)
            }
        }
        
        if (anomalies.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Developmental Anomalies", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(40f, 60f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        anomalies.forEach { (label, teeth) ->
            addTableRow(table, "$label:", teeth.sorted().joinToString(", "), labelFont, valueFont)
        }
        
        document.add(table)
    }
    
    private fun addPathologicalConditionsSection(document: Document, patient: Patient) {
        val state = patient.dentalState
        
        // Collect all pathological conditions
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            when {
                tooth.caries.contains("ICDAS 1") -> conditions.getOrPut("ICDAS 1") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("ICDAS 2") -> conditions.getOrPut("ICDAS 2") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("ICDAS 3") -> conditions.getOrPut("ICDAS 3") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("ICDAS 4") -> conditions.getOrPut("ICDAS 4") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("ICDAS 5") -> conditions.getOrPut("ICDAS 5") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("ICDAS 6") -> conditions.getOrPut("ICDAS 6") { mutableListOf() }.add(tooth.number)
                tooth.caries.contains("Root caries") -> conditions.getOrPut("Root Caries") { mutableListOf() }.add(tooth.number)
            }
            
            if (tooth.erosion.contains("+1") || tooth.erosion.contains("+2") || tooth.erosion.contains("+3")) {
                conditions.getOrPut("Erosion") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.fractureType.contains("+") && (tooth.fractureType.contains("Ellis") || tooth.fractureType.contains("fracture"))) {
                conditions.getOrPut("Fracture") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.attrition.contains("+")) {
                conditions.getOrPut("Attrition") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.abrasion.contains("+")) {
                conditions.getOrPut("Abrasion") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.abfraction.contains("+")) {
                conditions.getOrPut("Abfraction") { mutableListOf() }.add(tooth.number)
            }
            if (!tooth.discoloration.contains("0 =") && tooth.discoloration.contains("+")) {
                conditions.getOrPut("Discoloration") { mutableListOf() }.add(tooth.number)
            }
        }
        
        if (conditions.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Pathological & Traumatic Conditions", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(40f, 60f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        conditions.forEach { (label, teeth) ->
            addTableRow(table, "$label:", teeth.sorted().joinToString(", "), labelFont, valueFont)
        }
        
        document.add(table)
    }
    
    private fun addPulpVitalitySection(document: Document, patient: Patient) {
        val state = patient.dentalState
        
        // Collect pulp vitality conditions
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.painStatus.contains("reversible pulpitis")) {
                conditions.getOrPut("Reversible Pulpitis") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.painStatus.contains("irreversible")) {
                conditions.getOrPut("Irreversible Pulpitis") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.painStatus.contains("necrotic")) {
                conditions.getOrPut("Necrotic Pulp") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.tenderOnPercussion.contains("+1")) {
                conditions.getOrPut("Tender on Percussion") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.vestibularTenderness.contains("+1")) {
                conditions.getOrPut("Vestibular Tenderness") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.sinusTract.contains("+1") || tooth.sinusTract.contains("+2")) {
                conditions.getOrPut("Sinus Tract") { mutableListOf() }.add(tooth.number)
            }
        }
        
        if (conditions.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Pulp Vitality & Pain Conditions", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(40f, 60f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        conditions.forEach { (label, teeth) ->
            addTableRow(table, "$label:", teeth.sorted().joinToString(", "), labelFont, valueFont)
        }
        
        document.add(table)
    }
    
    private fun addPeriodontalParametersSection(document: Document, patient: Patient) {
        val state = patient.dentalState
        
        // Collect periodontal conditions
        val conditions = mutableMapOf<String, MutableList<Int>>()
        
        state.teeth.values.forEach { tooth ->
            if (tooth.mobility != "None") {
                conditions.getOrPut("Mobility - ${tooth.mobility}") { mutableListOf() }.add(tooth.number)
            }
            if (!tooth.pocket.contains("Normal")) {
                conditions.getOrPut("Pocket - ${tooth.pocket}") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.recession != "None") {
                conditions.getOrPut("Recession - ${tooth.recession}") { mutableListOf() }.add(tooth.number)
            }
            if (tooth.furcation != "None") {
                conditions.getOrPut("Furcation - ${tooth.furcation}") { mutableListOf() }.add(tooth.number)
            }
        }
        
        if (conditions.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Periodontal Parameters", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(40f, 60f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        conditions.forEach { (label, teeth) ->
            addTableRow(table, "$label:", teeth.sorted().joinToString(", "), labelFont, valueFont)
        }
        
        document.add(table)
    }
    
    private fun addGingivalSegmentTable(document: Document, patient: Patient) {
        val segments = patient.dentalState.gingivalSegments
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Gingival Segment Assessment", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        // Create table with 7 columns (1 for parameter label + 6 for segments)
        val table = PdfPTable(7)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2.5f, 1f, 1f, 1f, 1f, 1f, 1f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
        val cellFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, textColor)
        val labelFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, textColor)
        
        // Add header row
        val headerCell = PdfPCell(Phrase("Parameter", headerFont))
        headerCell.backgroundColor = primaryColor
        headerCell.setPadding(8f)
        headerCell.horizontalAlignment = Element.ALIGN_LEFT
        table.addCell(headerCell)
        
        for (i in 1..6) {
            val segmentCell = PdfPCell(Phrase("Seg $i", headerFont))
            segmentCell.backgroundColor = primaryColor
            segmentCell.setPadding(8f)
            segmentCell.horizontalAlignment = Element.ALIGN_CENTER
            table.addCell(segmentCell)
        }
        
        // Parameters to display
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
        
        // Add data rows
        parameters.forEachIndexed { index, (paramName, getter) ->
            val labelCell = PdfPCell(Phrase(paramName, labelFont))
            labelCell.setPadding(6f)
            labelCell.horizontalAlignment = Element.ALIGN_LEFT
            labelCell.backgroundColor = if (index % 2 == 0) lightGray else BaseColor.WHITE
            table.addCell(labelCell)
            
            for (segNum in 1..6) {
                val segment = segments[segNum]
                val value = segment?.let { getter(it) } ?: "-"
                val valueCell = PdfPCell(Phrase(value, cellFont))
                valueCell.setPadding(6f)
                valueCell.horizontalAlignment = Element.ALIGN_CENTER
                valueCell.backgroundColor = if (index % 2 == 0) lightGray else BaseColor.WHITE
                table.addCell(valueCell)
            }
        }
        
        document.add(table)
    }
    
    private fun addLesionCharacteristicsSection(document: Document, patient: Patient) {
        val info = patient.dentalState.patientInfo
        
        // Only add if there are lesion characteristics
        if (info.oralMucosa == "No issues" || info.lesionType.contains("Normal mucosa")) {
            return
        }
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Lesion Characteristics", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(30f, 70f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val labelFont = Font(Font.FontFamily.HELVETICA, 11f, Font.BOLD, textColor)
        val valueFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        
        if (!info.lesionType.contains("Normal mucosa")) {
            addTableRow(table, "Lesion Type:", info.lesionType, labelFont, valueFont)
        }
        if (!info.lesionSize.contains("0 =")) {
            addTableRow(table, "Lesion Size:", info.lesionSize, labelFont, valueFont)
        }
        if (!info.lesionColor.contains("Normal mucosa color")) {
            addTableRow(table, "Lesion Color:", info.lesionColor, labelFont, valueFont)
        }
        if (!info.surfaceTexture.contains("Normal texture")) {
            addTableRow(table, "Surface Texture:", info.surfaceTexture, labelFont, valueFont)
        }
        if (!info.borderDefinition.contains("Moderately defined")) {
            addTableRow(table, "Border Definition:", info.borderDefinition, labelFont, valueFont)
        }
        if (!info.lesionPain.contains("No pain")) {
            addTableRow(table, "Pain:", info.lesionPain, labelFont, valueFont)
        }
        if (!info.lesionDuration.contains("0 =")) {
            addTableRow(table, "Duration:", info.lesionDuration, labelFont, valueFont)
        }
        if (!info.burningSensation.contains("No burning")) {
            addTableRow(table, "Burning Sensation:", info.burningSensation, labelFont, valueFont)
        }
        if (!info.bleedingTendency.contains("No bleeding")) {
            addTableRow(table, "Bleeding Tendency:", info.bleedingTendency, labelFont, valueFont)
        }
        
        if (table.rows.size > 0) {
            document.add(table)
        }
    }
    
    private fun addToothStatusSection(document: Document, patient: Patient) {
        val state = patient.dentalState
        val abnormalTeeth = state.teeth.values.filter { 
            (it.status != "normal" && it.status != "not examined") ||
            (it.caries.contains("ICDAS") && !it.caries.contains("ICDAS 0"))
        }
        
        if (abnormalTeeth.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Detailed Tooth Status", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(15f, 25f, 30f, 30f))
        table.spacingBefore = 5f
        
        // Header row
        val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
        val headerBg = primaryColor
        
        addTableHeaderCell(table, "Tooth", headerFont, headerBg)
        addTableHeaderCell(table, "Status", headerFont, headerBg)
        addTableHeaderCell(table, "Periodontal", headerFont, headerBg)
        addTableHeaderCell(table, "Notes", headerFont, headerBg)
        
        // Data rows
        val dataFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL, textColor)
        abnormalTeeth.sortedBy { it.number }.forEach { tooth ->
            val cell1 = PdfPCell(Phrase(tooth.number.toString(), dataFont))
            cell1.setPadding(6f)
            cell1.backgroundColor = lightGray
            table.addCell(cell1)
            
            val cell2 = PdfPCell(Phrase(tooth.status, dataFont))
            cell2.setPadding(6f)
            table.addCell(cell2)
            
            val cell3 = PdfPCell(Phrase(tooth.periodontalStatus, dataFont))
            cell3.setPadding(6f)
            table.addCell(cell3)
            
            val notes = mutableListOf<String>()
            if (tooth.caries.contains("ICDAS") && !tooth.caries.contains("ICDAS 0")) {
                notes.add(tooth.caries.substringBefore(" - "))
            }
            if (tooth.treatmentType.isNotEmpty() && !tooth.status.equals("missing", ignoreCase = true)) {
                notes.add(tooth.treatmentType)
            }
            
            val cell4 = PdfPCell(Phrase(notes.joinToString("; "), dataFont))
            cell4.setPadding(6f)
            table.addCell(cell4)
        }
        
        document.add(table)
    }
    
    private fun addTableRow(table: PdfPTable, label: String, value: String, labelFont: Font, valueFont: Font) {
        val labelCell = PdfPCell(Phrase(label, labelFont))
        labelCell.setPadding(8f)
        labelCell.backgroundColor = lightGray
        labelCell.border = Rectangle.BOX
        labelCell.borderColor = BaseColor.GRAY
        labelCell.borderWidth = 0.5f
        table.addCell(labelCell)
        
        val valueCell = PdfPCell(Phrase(value, valueFont))
        valueCell.setPadding(8f)
        valueCell.border = Rectangle.BOX
        valueCell.borderColor = BaseColor.GRAY
        valueCell.borderWidth = 0.5f
        table.addCell(valueCell)
    }
    
    private fun addTableHeaderCell(table: PdfPTable, text: String, font: Font, bgColor: BaseColor) {
        val cell = PdfPCell(Phrase(text, font))
        cell.setPadding(8f)
        cell.backgroundColor = bgColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.border = Rectangle.BOX
        cell.borderColor = BaseColor.GRAY
        cell.borderWidth = 0.5f
        table.addCell(cell)
    }
    
    private fun addHeader(writer: PdfWriter, document: Document) {
        val headerTable = PdfPTable(1)
        headerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
        headerTable.defaultCell.border = Rectangle.NO_BORDER
        headerTable.defaultCell.backgroundColor = primaryColor
        headerTable.defaultCell.setPadding(5f)
        
        val headerText = Phrase("Latha Multi-Speciality Dental Clinic - Amalapuram", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.WHITE))
        headerTable.addCell(headerText)
        headerTable.writeSelectedRows(0, -1, document.leftMargin(), document.pageSize.height - 20f, writer.directContent)
    }
    
    private fun addFooter(writer: PdfWriter, document: Document) {
        val footerTable = PdfPTable(1)
        footerTable.totalWidth = document.pageSize.width - document.leftMargin() - document.rightMargin()
        footerTable.defaultCell.border = Rectangle.NO_BORDER
        footerTable.defaultCell.setPadding(5f)
        
        val pageNumber = Phrase("Page ${writer.pageNumber}", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY))
        footerTable.addCell(pageNumber)
        footerTable.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 10f, writer.directContent)
    }
    
    private fun convertSvgToImage(svgContent: String, width: Int, height: Int): ByteArray? {
        return try {
            var processedSvg = svgContent.trim()
            if (!processedSvg.startsWith("<?xml")) {
                processedSvg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$processedSvg"
            }
            if (!processedSvg.contains("xmlns=")) {
                processedSvg = processedSvg.replaceFirst(
                    "<svg",
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\""
                )
            }
            
            val transcoder = PNGTranscoder()
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width.toFloat())
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height.toFloat())
            
            val input = TranscoderInput(StringReader(processedSvg))
            val outputStream = ByteArrayOutputStream()
            val output = TranscoderOutput(outputStream)
            
            transcoder.transcode(input, output)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun addFinalDiagnosisSection(document: Document, patient: Patient) {
        val finalDiagnosis = patient.dentalState.patientInfo.finalDiagnosis
        
        if (finalDiagnosis.isBlank()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Final Diagnosis", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val contentFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, textColor)
        val paragraph = Paragraph(finalDiagnosis, contentFont)
        paragraph.spacingAfter = 15f
        paragraph.spacingBefore = 5f
        paragraph.firstLineIndent = 10f
        document.add(paragraph)
    }
    
    private fun addTreatmentPlanSection(document: Document, patient: Patient) {
        val treatmentPlans = patient.dentalState.patientInfo.treatmentPlans
            .filter { it.cdtCode.isNotEmpty() || it.description.isNotEmpty() }
        
        if (treatmentPlans.isEmpty()) return
        
        val headingFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD, primaryColor)
        val heading = Paragraph("Treatment Plan", headingFont)
        heading.spacingBefore = 10f
        heading.spacingAfter = 12f
        document.add(heading)
        
        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(25f, 50f, 25f))
        table.spacingBefore = 5f
        table.spacingAfter = 15f
        
        val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)
        val headerBg = primaryColor
        
        addTableHeaderCell(table, "CDT Code", headerFont, headerBg)
        addTableHeaderCell(table, "Description", headerFont, headerBg)
        addTableHeaderCell(table, "Date", headerFont, headerBg)
        
        val cellFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL, textColor)
        treatmentPlans.forEachIndexed { index, plan ->
            val cdtDisplay = if (plan.cdtCode.isNotEmpty() && plan.cdtName.isNotEmpty()) {
                "${plan.cdtCode} - ${plan.cdtName}"
            } else if (plan.cdtCode.isNotEmpty()) {
                plan.cdtCode
            } else {
                "-"
            }
            
            val codeCell = PdfPCell(Phrase(cdtDisplay, cellFont))
            codeCell.setPadding(8f)
            codeCell.backgroundColor = if (index % 2 == 0) lightGray else BaseColor.WHITE
            table.addCell(codeCell)
            
            val descCell = PdfPCell(Phrase(plan.description.ifEmpty { "-" }, cellFont))
            descCell.setPadding(8f)
            descCell.backgroundColor = if (index % 2 == 0) lightGray else BaseColor.WHITE
            table.addCell(descCell)
            
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
            val timestampCell = PdfPCell(Phrase(timestampText, cellFont))
            timestampCell.setPadding(8f)
            timestampCell.backgroundColor = if (index % 2 == 0) lightGray else BaseColor.WHITE
            table.addCell(timestampCell)
        }
        
        document.add(table)
    }
}

actual fun createPdfReportGenerator(context: Any?): PdfReportGenerator {
    return PdfReportGenerator()
}
