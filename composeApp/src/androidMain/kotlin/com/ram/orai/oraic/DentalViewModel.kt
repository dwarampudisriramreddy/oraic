package com.ram.oraic

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PatientInfo(
    val dob: String = "",
    val dentition: String = "Permanent",
    val smokingStatus: String = "Never smoker",
    val diabeticStatus: String = "Normal",
    val oralHygiene: String = "Good",
    val oralMucosa: String = "No issues",
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

data class Tooth(
    val number: Int,
    val status: String = "healthy", // "healthy", "abnormal", "missing", "treated"
    val periodontalStatus: String = "Healthy", // "Healthy", "Gingivitis", "Periodontitis"
    val visualChanges: String = "None", // "None", "Cavity", "Filling", "Crown", "Calculus"
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

data class DentalState(
    val patientInfo: PatientInfo = PatientInfo(),
    val teeth: Map<Int, Tooth> = (1..48).associateWith { Tooth(it) },
    val selectedTooth: Int? = null,
    val gingivalSegments: Map<Int, GingivalSegment> = (1..6).associateWith { GingivalSegment(it) },
    val selectedSegment: Int? = 1
)

class DentalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DentalState())
    val uiState: StateFlow<DentalState> = _uiState.asStateFlow()

    fun updatePatientInfo(newInfo: PatientInfo) {
        _uiState.update { it.copy(patientInfo = newInfo) }
    }

    fun cycleToothStatus(toothNum: Int) {
        _uiState.update { currentState ->
            val currentTooth = currentState.teeth[toothNum] ?: return@update currentState
            val newStatus = when (currentTooth.status) {
                "healthy" -> "abnormal"
                "abnormal" -> "missing"
                "missing" -> "treated"
                "treated" -> "healthy"
                else -> "healthy"
            }
            val updatedTeeth = currentState.teeth.toMutableMap()
            updatedTeeth[toothNum] = currentTooth.copy(status = newStatus)
            currentState.copy(teeth = updatedTeeth)
        }
    }

    fun selectTooth(toothNum: Int?) {
        _uiState.update { it.copy(selectedTooth = toothNum) }
    }

    fun selectSegment(segmentNum: Int?) {
        _uiState.update { it.copy(selectedSegment = segmentNum) }
    }

    private fun updateToothProperty(toothNum: Int, update: (Tooth) -> Tooth) {
        _uiState.update { currentState ->
            val currentTooth = currentState.teeth[toothNum] ?: return@update currentState
            val updatedTeeth = currentState.teeth.toMutableMap()
            updatedTeeth[toothNum] = update(currentTooth)
            currentState.copy(teeth = updatedTeeth)
        }
    }

    private fun updateGingivalSegmentProperty(segmentNum: Int, update: (GingivalSegment) -> GingivalSegment) {
        _uiState.update { currentState ->
            val currentSegment = currentState.gingivalSegments[segmentNum] ?: return@update currentState
            val updatedSegments = currentState.gingivalSegments.toMutableMap()
            updatedSegments[segmentNum] = update(currentSegment)
            currentState.copy(gingivalSegments = updatedSegments)
        }
    }

    fun updateGingivalSizeEdema(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(sizeEdema = value) }
    }
    fun updateGingivalColor(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(color = value) }
    }
    fun updateGingivalShape(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(shape = value) }
    }
    fun updateGingivalContour(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(contour = value) }
    }
    fun updateGingivalTexture(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(texture = value) }
    }
    fun updateGingivalConsistency(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(consistency = value) }
    }
    fun updateGingivalDistribution(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(distribution = value) }
    }
    fun updateGingivalBleeding(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(bleeding = value) }
    }
    fun updateGingivalExudate(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(exudate = value) }
    }
    fun updateGingivalCalculus(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(calculus = value) }
    }
    fun updateGingivalPlaque(segmentNum: Int, value: String) {
        updateGingivalSegmentProperty(segmentNum) { it.copy(plaque = value) }
    }

    fun updateToothSize(toothNum: Int, size: String) {
        updateToothProperty(toothNum) { it.copy(toothSize = size) }
    }

    fun updateToothShape(toothNum: Int, shape: String) {
        updateToothProperty(toothNum) { it.copy(toothShape = shape) }
    }

    fun updateToothMorphology(toothNum: Int, morphology: String) {
        updateToothProperty(toothNum) { it.copy(toothMorphology = morphology) }
    }

    fun updateEruptionStatus(toothNum: Int, status: String) {
        updateToothProperty(toothNum) { it.copy(eruptionStatus = status) }
    }

    fun updateCaries(toothNum: Int, caries: String) {
        updateToothProperty(toothNum) { it.copy(caries = caries) }
    }

    fun updateErosion(toothNum: Int, erosion: String) {
        updateToothProperty(toothNum) { it.copy(erosion = erosion) }
    }

    fun updateFractureType(toothNum: Int, fractureType: String) {
        updateToothProperty(toothNum) { it.copy(fractureType = fractureType) }
    }

    fun updateAttrition(toothNum: Int, attrition: String) {
        updateToothProperty(toothNum) { it.copy(attrition = attrition) }
    }

    fun updateAbrasion(toothNum: Int, abrasion: String) {
        updateToothProperty(toothNum) { it.copy(abrasion = abrasion) }
    }

    fun updateAbfraction(toothNum: Int, abfraction: String) {
        updateToothProperty(toothNum) { it.copy(abfraction = abfraction) }
    }

    fun updateDiscoloration(toothNum: Int, discoloration: String) {
        updateToothProperty(toothNum) { it.copy(discoloration = discoloration) }
    }

    fun updatePainStatus(toothNum: Int, painStatus: String) {
        updateToothProperty(toothNum) { it.copy(painStatus = painStatus) }
    }

    fun updateTenderOnPercussion(toothNum: Int, tender: String) {
        updateToothProperty(toothNum) { it.copy(tenderOnPercussion = tender) }
    }

    fun updateVestibularTenderness(toothNum: Int, tenderness: String) {
        updateToothProperty(toothNum) { it.copy(vestibularTenderness = tenderness) }
    }

    fun updateSinusTract(toothNum: Int, sinusTract: String) {
        updateToothProperty(toothNum) { it.copy(sinusTract = sinusTract) }
    }

    fun updateRecession(toothNum: Int, recession: String) {
        updateToothProperty(toothNum) { it.copy(recession = recession) }
    }

    fun updatePocket(toothNum: Int, pocket: String) {
        updateToothProperty(toothNum) { it.copy(pocket = pocket) }
    }

    fun updateMobility(toothNum: Int, mobility: String) {
        updateToothProperty(toothNum) { it.copy(mobility = mobility) }
    }

    fun updateFurcation(toothNum: Int, furcation: String) {
        updateToothProperty(toothNum) { it.copy(furcation = furcation) }
    }
}
