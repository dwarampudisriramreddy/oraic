package com.ram.orai.oraic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DentalStateHolder {
    var uiState by mutableStateOf(DentalState())
        private set
    
    fun initializeWithDentalState(dentalState: DentalState) {
        uiState = dentalState
    }

    fun updatePatientInfo(newInfo: PatientInfo) {
        uiState = uiState.copy(patientInfo = newInfo)
    }

    fun cycleToothStatus(toothNum: Int) {
        val currentTooth = uiState.teeth[toothNum] ?: return
        val newStatus = when (currentTooth.status) {
            "healthy" -> "abnormal"
            "abnormal" -> "missing"
            "missing" -> "treated"
            "treated" -> "healthy"
            else -> "healthy"
        }
        val updatedTeeth = uiState.teeth.toMutableMap()
        updatedTeeth[toothNum] = currentTooth.copy(status = newStatus)
        uiState = uiState.copy(teeth = updatedTeeth)
    }

    fun updateToothStatus(toothNum: Int, status: String) {
        val currentTooth = uiState.teeth[toothNum] ?: Tooth(toothNum)
        val updatedTeeth = uiState.teeth.toMutableMap()
        
        // If status is changed to "treated" and treatment type is empty, set default to "restoration"
        val updatedTooth = if (status.equals("treated", ignoreCase = true) && currentTooth.treatmentType.isEmpty()) {
            currentTooth.copy(
                status = status,
                treatmentType = "restoration",
                treatmentStatus = if (currentTooth.treatmentStatus.isEmpty()) "good" else currentTooth.treatmentStatus
            )
        } else {
            currentTooth.copy(status = status)
        }
        
        updatedTeeth[toothNum] = updatedTooth
        uiState = uiState.copy(teeth = updatedTeeth)
    }

    fun selectTooth(toothNum: Int?) {
        uiState = uiState.copy(selectedTooth = toothNum)
    }

    fun selectSegment(segmentNum: Int?) {
        uiState = uiState.copy(selectedSegment = segmentNum)
    }

    private fun updateToothProperty(toothNum: Int, update: (Tooth) -> Tooth) {
        val currentTooth = uiState.teeth[toothNum] ?: return
        val updatedTeeth = uiState.teeth.toMutableMap()
        updatedTeeth[toothNum] = update(currentTooth)
        uiState = uiState.copy(teeth = updatedTeeth)
    }

    private fun updateGingivalSegmentProperty(segmentNum: Int, update: (GingivalSegment) -> GingivalSegment) {
        val currentSegment = uiState.gingivalSegments[segmentNum] ?: return
        val updatedSegments = uiState.gingivalSegments.toMutableMap()
        updatedSegments[segmentNum] = update(currentSegment)
        uiState = uiState.copy(gingivalSegments = updatedSegments)
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

    fun updateTreatmentType(toothNum: Int, treatmentType: String) {
        updateToothProperty(toothNum) { it.copy(treatmentType = treatmentType) }
    }

    fun updateTreatmentStatus(toothNum: Int, treatmentStatus: String) {
        updateToothProperty(toothNum) { it.copy(treatmentStatus = treatmentStatus) }
    }
    
    fun updateFinalDiagnosis(diagnosis: String) {
        uiState = uiState.copy(patientInfo = uiState.patientInfo.copy(finalDiagnosis = diagnosis))
    }
    
    fun updateTreatmentPlans(plans: List<TreatmentPlanItem>) {
        uiState = uiState.copy(patientInfo = uiState.patientInfo.copy(treatmentPlans = plans))
    }
}

