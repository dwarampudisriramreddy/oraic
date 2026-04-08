package com.ram.orai.oraic

import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to export patient data to CSV format
 */
class CsvExportHelper {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Escapes a CSV field value
     */
    private fun escapeCsvField(value: String): String {
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
    
    /**
     * Converts a list of patients to CSV format with all parameters
     */
    fun exportPatientsToCsv(patients: List<Patient>): String {
        val csv = StringBuilder()
        
        // Build comprehensive header
        val header = buildHeader()
        csv.appendLine(header)
        
        // CSV Rows
        patients.forEach { patient ->
            val row = buildPatientRow(patient)
            csv.appendLine(row.joinToString(","))
        }
        
        return csv.toString()
    }
    
    private fun buildHeader(): String {
        val headers = mutableListOf<String>()
        
        // Basic Patient Info
        headers.addAll(listOf(
            "ID", "Surname", "Given Name", "Phone Number", "Email", "Address",
            "Created At", "Updated At"
        ))
        
        // PatientInfo - Basic
        headers.addAll(listOf(
            "DOB", "Dentition", "Chief Complaint", "Chief Complaint Region", "Chief Complaint Since",
            "Smoking Status", "Diabetic Status", "Oral Hygiene", "Other Systemic Disease",
            "Oral Mucosa", "Malocclusion", "Number Status"
        ))
        
        // PatientInfo - Lesion Characteristics (1-20)
        headers.addAll(listOf(
            "Lesion Type", "Lesion Size", "Lesion Color", "Surface Texture", "Border Definition",
            "Induration", "Lesion Pain", "Burning Sensation", "Bleeding Tendency", "Lesion Duration",
            "Pattern Distribution", "Mobility Fixation", "Lymphadenopathy", "Associated Systemic Symptoms",
            "Removability", "Growth Rate", "Nikolsky Sign", "Wickhams Striae", "Recurrence Pattern",
            "Functional Impairment", "Matrix Formation", "Mineralization"
        ))
        
        // PatientInfo - Extra-oral Examination (25-44)
        headers.addAll(listOf(
            "Shape Of Head", "Facial Symmetry", "Facial Form", "Facial Divergence",
            "Interlabial Gap At Rest", "Nasolabial Angle", "Upper Lip Posture", "Lower Lip Posture",
            "Lip Tonicity", "Resting Lower Lip Line", "Mentolabial Sulcus", "Chin Position",
            "AP Skeletal Relationship", "Total Face Height", "Upper Face Height", "Middle Face Height",
            "Lower Face Height", "VTO", "Clinical FMPA", "Incisor Exposure At Rest",
            "Incisor Exposure At Speech", "Incisor Exposure On Smiling"
        ))
        
        // PatientInfo - Functional Examination (47-54)
        headers.addAll(listOf(
            "Respiratory Pattern", "Deglutition", "Mastication", "Speech",
            "Path Of Closure", "Lateral Path Of Closure", "TMJ Function", "Postural Rest Position Of Tongue"
        ))
        
        // PatientInfo - Maxillary Arch (55-61)
        headers.addAll(listOf(
            "Maxillary Arch Shape", "Maxillary Arch Symmetry", "Maxillary Crowding", "Maxillary Spacing",
            "Maxillary Rotation", "Maxillary Axial Inclination", "Maxillary Transposition"
        ))
        
        // PatientInfo - Mandibular Arch (62-68)
        headers.addAll(listOf(
            "Mandibular Arch Shape", "Mandibular Arch Symmetry", "Mandibular Crowding", "Mandibular Spacing",
            "Mandibular Rotation", "Mandibular Axial Inclination", "Mandibular Transposition"
        ))
        
        // PatientInfo - Inter-arch Relationship (69-80)
        headers.addAll(listOf(
            "Maximum Mouth Opening", "Freeway Space", "Curve Of Spee", "Molar Relationship",
            "Canine Relationship", "Incisor Relationship", "Overjet", "Overbite",
            "Crossbite Type", "Upper Dental Midline", "Lower Dental Midline", "Inter Arch Midline Discrepancy"
        ))
        
        // Selected tooth and segment
        headers.addAll(listOf("Selected Tooth", "Selected Segment"))
        
        // Tooth data - for each tooth (1-48), add all fields
        for (toothNum in 1..48) {
            headers.addAll(listOf(
                "Tooth_${toothNum}_Status", "Tooth_${toothNum}_PeriodontalStatus", "Tooth_${toothNum}_VisualChanges",
                "Tooth_${toothNum}_TreatmentType", "Tooth_${toothNum}_TreatmentStatus",
                "Tooth_${toothNum}_ToothSize", "Tooth_${toothNum}_ToothShape", "Tooth_${toothNum}_ToothMorphology",
                "Tooth_${toothNum}_EruptionStatus", "Tooth_${toothNum}_Caries", "Tooth_${toothNum}_Erosion",
                "Tooth_${toothNum}_FractureType", "Tooth_${toothNum}_Attrition", "Tooth_${toothNum}_Abrasion",
                "Tooth_${toothNum}_Abfraction", "Tooth_${toothNum}_Discoloration", "Tooth_${toothNum}_PainStatus",
                "Tooth_${toothNum}_TenderOnPercussion", "Tooth_${toothNum}_VestibularTenderness",
                "Tooth_${toothNum}_SinusTract", "Tooth_${toothNum}_Recession", "Tooth_${toothNum}_Pocket",
                "Tooth_${toothNum}_Mobility", "Tooth_${toothNum}_Furcation"
            ))
        }
        
        // Gingival Segment data - for each segment (1-6), add all fields
        for (segmentNum in 1..6) {
            headers.addAll(listOf(
                "Segment_${segmentNum}_SizeEdema", "Segment_${segmentNum}_Color", "Segment_${segmentNum}_Shape",
                "Segment_${segmentNum}_Contour", "Segment_${segmentNum}_Texture", "Segment_${segmentNum}_Consistency",
                "Segment_${segmentNum}_Distribution", "Segment_${segmentNum}_Bleeding", "Segment_${segmentNum}_Exudate",
                "Segment_${segmentNum}_Calculus", "Segment_${segmentNum}_Plaque"
            ))
        }
        
        return headers.joinToString(",")
    }
    
    private fun buildPatientRow(patient: Patient): List<String> {
        val row = mutableListOf<String>()
        val info = patient.dentalState.patientInfo
        
        // Format dates safely - handle 0 or invalid timestamps
        // Check if timestamp is reasonable (after year 2000, before year 2100)
        val minValidTimestamp = 946684800000L // Jan 1, 2000
        val maxValidTimestamp = 4102444800000L // Jan 1, 2100
        
        val createdDateStr = if (patient.createdAt >= minValidTimestamp && patient.createdAt <= maxValidTimestamp) {
            try {
                dateFormat.format(Date(patient.createdAt))
            } catch (e: Exception) {
                patient.createdAt.toString() // Fallback to raw timestamp
            }
        } else if (patient.createdAt > 0) {
            // If timestamp exists but is outside reasonable range, format it anyway
            try {
                dateFormat.format(Date(patient.createdAt))
            } catch (e: Exception) {
                patient.createdAt.toString()
            }
        } else {
            "" // Empty if timestamp is 0 or negative
        }
        
        val updatedDateStr = if (patient.updatedAt >= minValidTimestamp && patient.updatedAt <= maxValidTimestamp) {
            try {
                dateFormat.format(Date(patient.updatedAt))
            } catch (e: Exception) {
                patient.updatedAt.toString() // Fallback to raw timestamp
            }
        } else if (patient.updatedAt > 0) {
            // If timestamp exists but is outside reasonable range, format it anyway
            try {
                dateFormat.format(Date(patient.updatedAt))
            } catch (e: Exception) {
                patient.updatedAt.toString()
            }
        } else {
            "" // Empty if timestamp is 0 or negative
        }
        
        // Basic Patient Info
        row.addAll(listOf(
            escapeCsvField(patient.id),
            escapeCsvField(patient.surname),
            escapeCsvField(patient.givenName),
            escapeCsvField(patient.phoneNumber),
            escapeCsvField(patient.email),
            escapeCsvField(patient.address),
            escapeCsvField(createdDateStr),
            escapeCsvField(updatedDateStr)
        ))
        
        // PatientInfo - Basic
        row.addAll(listOf(
            escapeCsvField(info.dob),
            escapeCsvField(info.dentition),
            escapeCsvField(info.chiefComplaint),
            escapeCsvField(info.chiefComplaintRegion),
            escapeCsvField(info.chiefComplaintSince),
            escapeCsvField(info.smokingStatus),
            escapeCsvField(info.diabeticStatus),
            escapeCsvField(info.oralHygiene),
            escapeCsvField(info.otherSystemicDisease),
            escapeCsvField(info.oralMucosa),
            escapeCsvField(info.malocclusion),
            escapeCsvField(info.numberStatus)
        ))
        
        // PatientInfo - Lesion Characteristics
        row.addAll(listOf(
            escapeCsvField(info.lesionType), escapeCsvField(info.lesionSize), escapeCsvField(info.lesionColor),
            escapeCsvField(info.surfaceTexture), escapeCsvField(info.borderDefinition), escapeCsvField(info.induration),
            escapeCsvField(info.lesionPain), escapeCsvField(info.burningSensation), escapeCsvField(info.bleedingTendency),
            escapeCsvField(info.lesionDuration), escapeCsvField(info.patternDistribution), escapeCsvField(info.mobilityFixation),
            escapeCsvField(info.lymphadenopathy), escapeCsvField(info.associatedSystemicSymptoms), escapeCsvField(info.removability),
            escapeCsvField(info.growthRate), escapeCsvField(info.nikolskySign), escapeCsvField(info.wickhamsStriae),
            escapeCsvField(info.recurrencePattern), escapeCsvField(info.functionalImpairment), escapeCsvField(info.matrixFormation),
            escapeCsvField(info.mineralization)
        ))
        
        // PatientInfo - Extra-oral Examination
        row.addAll(listOf(
            escapeCsvField(info.shapeOfHead), escapeCsvField(info.facialSymmetry), escapeCsvField(info.facialForm),
            escapeCsvField(info.facialDivergence), escapeCsvField(info.interlabialGapAtRest), escapeCsvField(info.nasolabialAngle),
            escapeCsvField(info.upperLipPosture), escapeCsvField(info.lowerLipPosture), escapeCsvField(info.lipTonicity),
            escapeCsvField(info.restingLowerLipLine), escapeCsvField(info.mentolabialSulcus), escapeCsvField(info.chinPosition),
            escapeCsvField(info.apSkeletalRelationship), escapeCsvField(info.totalFaceHeight), escapeCsvField(info.upperFaceHeight),
            escapeCsvField(info.middleFaceHeight), escapeCsvField(info.lowerFaceHeight), escapeCsvField(info.vto),
            escapeCsvField(info.clinicalFmpa), escapeCsvField(info.incisorExposureAtRest), escapeCsvField(info.incisorExposureAtSpeech),
            escapeCsvField(info.incisorExposureOnSmiling)
        ))
        
        // PatientInfo - Functional Examination
        row.addAll(listOf(
            escapeCsvField(info.respiratoryPattern), escapeCsvField(info.deglutition), escapeCsvField(info.mastication),
            escapeCsvField(info.speech), escapeCsvField(info.pathOfClosure), escapeCsvField(info.lateralPathOfClosure),
            escapeCsvField(info.tmjFunction), escapeCsvField(info.posturalRestPositionOfTongue)
        ))
        
        // PatientInfo - Maxillary Arch
        row.addAll(listOf(
            escapeCsvField(info.maxillaryArchShape), escapeCsvField(info.maxillaryArchSymmetry), escapeCsvField(info.maxillaryCrowding),
            escapeCsvField(info.maxillarySpacing), escapeCsvField(info.maxillaryRotation), escapeCsvField(info.maxillaryAxialInclination),
            escapeCsvField(info.maxillaryTransposition)
        ))
        
        // PatientInfo - Mandibular Arch
        row.addAll(listOf(
            escapeCsvField(info.mandibularArchShape), escapeCsvField(info.mandibularArchSymmetry), escapeCsvField(info.mandibularCrowding),
            escapeCsvField(info.mandibularSpacing), escapeCsvField(info.mandibularRotation), escapeCsvField(info.mandibularAxialInclination),
            escapeCsvField(info.mandibularTransposition)
        ))
        
        // PatientInfo - Inter-arch Relationship
        row.addAll(listOf(
            escapeCsvField(info.maximumMouthOpening), escapeCsvField(info.freewaySpace), escapeCsvField(info.curveOfSpee),
            escapeCsvField(info.molarRelationship), escapeCsvField(info.canineRelationship), escapeCsvField(info.incisorRelationship),
            escapeCsvField(info.overjet), escapeCsvField(info.overbite), escapeCsvField(info.crossbiteType),
            escapeCsvField(info.upperDentalMidline), escapeCsvField(info.lowerDentalMidline), escapeCsvField(info.interArchMidlineDiscrepancy)
        ))
        
        // Selected tooth and segment
        row.addAll(listOf(
            escapeCsvField(patient.dentalState.selectedTooth?.toString() ?: ""),
            escapeCsvField(patient.dentalState.selectedSegment?.toString() ?: "")
        ))
        
        // Tooth data - for each tooth (1-48)
        for (toothNum in 1..48) {
            val tooth = patient.dentalState.teeth[toothNum] ?: Tooth(toothNum)
            row.addAll(listOf(
                escapeCsvField(tooth.status),
                escapeCsvField(tooth.periodontalStatus),
                escapeCsvField(tooth.visualChanges),
                escapeCsvField(tooth.treatmentType),
                escapeCsvField(tooth.treatmentStatus),
                escapeCsvField(tooth.toothSize),
                escapeCsvField(tooth.toothShape),
                escapeCsvField(tooth.toothMorphology),
                escapeCsvField(tooth.eruptionStatus),
                escapeCsvField(tooth.caries),
                escapeCsvField(tooth.erosion),
                escapeCsvField(tooth.fractureType),
                escapeCsvField(tooth.attrition),
                escapeCsvField(tooth.abrasion),
                escapeCsvField(tooth.abfraction),
                escapeCsvField(tooth.discoloration),
                escapeCsvField(tooth.painStatus),
                escapeCsvField(tooth.tenderOnPercussion),
                escapeCsvField(tooth.vestibularTenderness),
                escapeCsvField(tooth.sinusTract),
                escapeCsvField(tooth.recession),
                escapeCsvField(tooth.pocket),
                escapeCsvField(tooth.mobility),
                escapeCsvField(tooth.furcation)
            ))
        }
        
        // Gingival Segment data - for each segment (1-6)
        for (segmentNum in 1..6) {
            val segment = patient.dentalState.gingivalSegments[segmentNum] ?: GingivalSegment(segmentNum)
            row.addAll(listOf(
                escapeCsvField(segment.sizeEdema),
                escapeCsvField(segment.color),
                escapeCsvField(segment.shape),
                escapeCsvField(segment.contour),
                escapeCsvField(segment.texture),
                escapeCsvField(segment.consistency),
                escapeCsvField(segment.distribution),
                escapeCsvField(segment.bleeding),
                escapeCsvField(segment.exudate),
                escapeCsvField(segment.calculus),
                escapeCsvField(segment.plaque)
            ))
        }
        
        return row
    }
    
    /**
     * Generates a filename for the CSV export
     */
    fun generateCsvFileName(): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "ORAIC_Patients_Export_$dateStr.csv"
    }
}
