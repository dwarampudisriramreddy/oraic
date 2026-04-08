package com.ram.orai.oraic

import kotlinx.serialization.Serializable

@Serializable
data class TreatmentPlanItem(
    val id: String = "",
    val cdtCode: String = "",
    val cdtName: String = "",
    val description: String = "",
    val timestamp: String = ""
)

@Serializable
data class PatientInfo(
    val dob: String = "",
    val dentition: String = "Permanent",
    val chiefComplaint: String = "",
    val chiefComplaintRegion: String = "",
    val chiefComplaintSince: String = "",
    val smokingStatus: String = "Never smoker",
    val diabeticStatus: String = "Normal",
    val oralHygiene: String = "Good",
    val otherSystemicDisease: String = "",
    val oralMucosa: String = "No issues",
    val finalDiagnosis: String = "",
    val treatmentPlans: List<TreatmentPlanItem> = emptyList(),
    // Lesion Characteristics Parameters (1-20)
    val lesionType: String = "0 = Normal mucosa (smooth, intact)",
    val lesionSize: String = "0 = 10mm (1cm)",
    val lesionColor: String = "0 = Normal mucosa color (coral pink)",
    val surfaceTexture: String = "0 = Normal texture",
    val borderDefinition: String = "0 = Moderately defined",
    val induration: String = "0 = Normal consistency",
    val lesionPain: String = "0 = No pain (asymptomatic)",
    val burningSensation: String = "0 = No burning",
    val bleedingTendency: String = "0 = No bleeding",
    val lesionDuration: String = "0 = 1-2 months",
    val patternDistribution: String = "0 = Solitary (single isolated lesion)",
    val mobilityFixation: String = "0 = Normal mobility",
    val lymphadenopathy: String = "0 = No palpable nodes",
    val associatedSystemicSymptoms: String = "0 = No systemic symptoms",
    val removability: String = "0 = Not applicable (no coating)",
    val growthRate: String = "0 = Stable (no change)",
    val nikolskySign: String = "0 = Negative (epithelium intact)",
    val wickhamsStriae: String = "0 = Absent (no white lines)",
    val recurrencePattern: String = "0 = No recurrence data",
    val functionalImpairment: String = "0 = No impairment",
    val malocclusion: String = "None",
    val matrixFormation: String = "0 = Normal matrix formation",
    val mineralization: String = "0.7-1.0 = Normal mineralization",
    val numberStatus: String = "0 = Normal dentition",
    // Extra-oral Examination Parameters (25-44)
    val shapeOfHead: String = "0 = Mesocephalic",
    val facialSymmetry: String = "0 = Symmetric",
    val facialForm: String = "0 = Straight",
    val facialDivergence: String = "0 = Normal",
    val interlabialGapAtRest: String = "0 = Competent (lips seal)",
    val nasolabialAngle: String = "0 = Normal (90-105°)",
    val upperLipPosture: String = "0 = Normal length",
    val lowerLipPosture: String = "0 = Normal length",
    val lipTonicity: String = "0 = Normal",
    val restingLowerLipLine: String = "0 = Average (at gingival margin)",
    val mentolabialSulcus: String = "0 = Average (3-5mm)",
    val chinPosition: String = "0 = Normal",
    val apSkeletalRelationship: String = "0 = Skeletal Class I",
    val totalFaceHeight: String = "0 = Normal",
    val upperFaceHeight: String = "0 = Normal",
    val middleFaceHeight: String = "0 = Normal",
    val lowerFaceHeight: String = "0 = Normal (50-55%)",
    val vto: String = "0 = Neutral",
    val clinicalFmpa: String = "0 = Average growth pattern",
    val incisorExposureAtRest: String = "0 = Normal (1-2mm)",
    val incisorExposureAtSpeech: String = "0 = Normal",
    val incisorExposureOnSmiling: String = "0 = Normal (0-1mm)",
    // Functional Examination Parameters (47-54)
    val respiratoryPattern: String = "0 = Nasal breathing",
    val deglutition: String = "0 = Normal mature swallow",
    val mastication: String = "0 = Normal bilateral",
    val speech: String = "0 = Normal articulation",
    val pathOfClosure: String = "0 = Normal",
    val lateralPathOfClosure: String = "0 = Normal",
    val tmjFunction: String = "0 = Normal",
    val posturalRestPositionOfTongue: String = "0 = Normal (against palate)",
    // Maxillary Arch Parameters (55-61)
    val maxillaryArchShape: String = "0 = Average/U-shaped",
    val maxillaryArchSymmetry: String = "0 = Symmetric",
    val maxillaryCrowding: String = "0 = No crowding (≥0mm)",
    val maxillarySpacing: String = "0 = No spacing",
    val maxillaryRotation: String = "0 = No rotation",
    val maxillaryAxialInclination: String = "0 = Normal",
    val maxillaryTransposition: String = "0 = No transposition",
    // Mandibular Arch Parameters (62-68)
    val mandibularArchShape: String = "0 = Average/U-shaped",
    val mandibularArchSymmetry: String = "0 = Symmetric",
    val mandibularCrowding: String = "0 = No crowding (≥0mm)",
    val mandibularSpacing: String = "0 = No spacing",
    val mandibularRotation: String = "0 = No rotation",
    val mandibularAxialInclination: String = "0 = Normal",
    val mandibularTransposition: String = "0 = No transposition",
    // Inter-arch Relationship Parameters (69-80)
    val maximumMouthOpening: String = "0 = Normal (35-45mm)",
    val freewaySpace: String = "0 = Normal (2-4mm)",
    val curveOfSpee: String = "0 = Normal/Flat (0-2mm)",
    val molarRelationship: String = "0 = Class I (normal)",
    val canineRelationship: String = "0 = Class I canine",
    val incisorRelationship: String = "0 = Class I normal",
    val overjet: String = "0 = Normal (2-3mm)",
    val overbite: String = "0 = Normal (20-40%)",
    val crossbiteType: String = "0 = No crossbite",
    val upperDentalMidline: String = "0 = Coincident with facial midline",
    val lowerDentalMidline: String = "0 = Coincident with facial midline",
    val interArchMidlineDiscrepancy: String = "0 = Coincident midlines"
)

@Serializable
data class GingivalSegment(
    val number: Int,
    val sizeEdema: String = "Normal",
    val color: String = "Pink",
    val shape: String = "Normal (scalloped)",
    val contour: String = "Normal (pointed)",
    val texture: String = "Stippled (normal)",
    val consistency: String = "Firm and resilient",
    val distribution: String = "None",
    val bleeding: String = "No",
    val exudate: String = "None",
    val calculus: String = "None",
    val plaque: String = "None"
)

@Serializable
data class Tooth(
    val number: Int,
    val status: String = "normal", // "normal", "not examined", "abnormal", "missing", "treated"
    val periodontalStatus: String = "Healthy", // "Healthy", "Gingivitis", "Periodontitis"
    val visualChanges: String = "None", // "None", "Cavity", "Filling", "Crown", "Calculus"
    val treatmentType: String = "", // "restoration", "root canal", "implant", "prosthesis"
    val treatmentStatus: String = "", // "good", "failed"
    val toothSize: String = "0 = Normal (±1.5mm)",
    val toothShape: String = "0 = Normal shape",
    val toothMorphology: String = "0 = Normal morphology",
    val eruptionStatus: String = "0 = Normal eruption",
    val caries: String = "0 = ICDAS 0 - Sound tooth",
    val erosion: String = "0 = No erosive wear",
    val fractureType: String = "0 = No fracture",
    val attrition: String = "0 = No attrition",
    val abrasion: String = "0 = No abrasion",
    val abfraction: String = "0 = No abfraction",
    val discoloration: String = "0 = Normal tooth color",
    val painStatus: String = "0 = No pain (asymptomatic)",
    val tenderOnPercussion: String = "0 = Negative",
    val vestibularTenderness: String = "0 = Negative",
    val sinusTract: String = "0 = Normal (no sinus tract)",
    val recession: String = "None",
    val pocket: String = "Normal 1-3mm",
    val mobility: String = "None",
    val furcation: String = "None"
)

@Serializable
data class DentalStateSerializable(
    val patientInfo: PatientInfo = PatientInfo(),
    val teeth: List<Tooth> = ((1..48) + (51..55) + (61..65) + (71..75) + (81..85)).map { Tooth(it) },
    val selectedTooth: Int? = null,
    val gingivalSegments: List<GingivalSegment> = (1..6).map { GingivalSegment(it) },
    val selectedSegment: Int? = 1
) {
    fun toDentalState(): DentalState {
        return DentalState(
            patientInfo = patientInfo,
            teeth = teeth.associateBy { it.number },
            selectedTooth = selectedTooth,
            gingivalSegments = gingivalSegments.associateBy { it.number },
            selectedSegment = selectedSegment
        )
    }
    
    companion object {
        fun fromDentalState(state: DentalState): DentalStateSerializable {
            return DentalStateSerializable(
                patientInfo = state.patientInfo,
                teeth = state.teeth.values.sortedBy { it.number },
                selectedTooth = state.selectedTooth,
                gingivalSegments = state.gingivalSegments.values.sortedBy { it.number },
                selectedSegment = state.selectedSegment
            )
        }
    }
}

data class DentalState(
    val patientInfo: PatientInfo = PatientInfo(),
    val teeth: Map<Int, Tooth> = ((1..48) + (51..55) + (61..65) + (71..75) + (81..85)).associateWith { Tooth(it) },
    val selectedTooth: Int? = null,
    val gingivalSegments: Map<Int, GingivalSegment> = (1..6).associateWith { GingivalSegment(it) },
    val selectedSegment: Int? = 1
)

