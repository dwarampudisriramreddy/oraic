package com.ram.orai.oraic

/**
 * Complete Periodontal Fuzzy Diagnostic System
 * Based on gingival parameters and ICD-11 classification
 */

data class PeriodontalDiagnosis(
    val segmentNumber: Int? = null, // null for overall diagnosis
    val primaryDiagnosis: PeriodontalDiseaseVector?,
    val differentialDiagnoses: List<PeriodontalDiseaseVector>,
    val icd11Codes: List<String>
)

data class PeriodontalDiseaseVector(
    val id: Int,
    val name: String,
    val icd11Primary: String,
    val icd11Secondary: String = "",
    val probability: Double,
    val matchedFeatures: List<String>,
    val cosineSimilarity: Double = 0.0,
    val symbolicMatch: Boolean = false
)

data class SeparatePeriodontalDiagnosisResults(
    val featureVector: Map<Int, Double>,              // Actual feature map values
    val distanceResults: List<PeriodontalDiseaseVector>,      // Cosine similarity results (top 5)
    val bayesianResults: List<PeriodontalDiseaseVector>,       // Bayesian probability results (top 5)
    val symbolicResults: List<PeriodontalDiseaseVector>,      // Symbolic logic results (top 5)
    val integratedDiagnosis: PeriodontalDiseaseVector?       // Final integrated diagnosis
)

class PeriodontalDiagnosticEngine {
    
    /**
     * Diagnose overall periodontal condition based on all segments and tooth-level data
     */
    fun diagnoseOverall(
        segments: Map<Int, GingivalSegment>,
        patientInfo: PatientInfo,
        teeth: Map<Int, Tooth>
    ): PeriodontalDiagnosis {
        // First, apply symbolic logic based on tooth-level periodontal parameters
        val symbolicDiagnosis = applySymbolicLogicForPeriodontitis(teeth, patientInfo)
        if (symbolicDiagnosis != null) {
            return symbolicDiagnosis
        }
        
        // Aggregate features from all segments
        val aggregatedFeatures = aggregateSegmentFeatures(segments, patientInfo, teeth)
        
        return diagnoseFromFeatures(aggregatedFeatures, null)
    }
    
    /**
     * Get separate diagnosis results for each method and integrated diagnosis
     */
    fun getSeparateDiagnosisResults(
        segments: Map<Int, GingivalSegment>,
        patientInfo: PatientInfo,
        teeth: Map<Int, Tooth>
    ): SeparatePeriodontalDiagnosisResults {
        // Get aggregated features
        val featureMap = aggregateSegmentFeatures(segments, patientInfo, teeth)
        
        // Convert feature map to vector for cosine similarity (13 features)
        val featureVector = (1..13).map { featureMap[it] ?: 0.0 }
        
        // 1. Distance-based analysis (Cosine Similarity) - 50% weight
        val distanceResults = (1..PERIODONTAL_DISEASE_VECTORS.size).map { vectorId ->
            val diseaseVector = getDiseaseVectorForCosine(vectorId)
            val similarity = calculateCosineSimilarity(featureVector, diseaseVector)
            val vectorInfo = PERIODONTAL_DISEASE_VECTORS[vectorId] ?: PeriodontalDiseaseInfo("Unknown", "", "")
            PeriodontalDiseaseVector(
                id = vectorId,
                name = vectorInfo.name,
                icd11Primary = vectorInfo.icd11Primary,
                icd11Secondary = vectorInfo.icd11Secondary,
                probability = 0.0,
                matchedFeatures = emptyList(),
                cosineSimilarity = similarity,
                symbolicMatch = false
            )
        }.sortedByDescending { it.cosineSimilarity }.take(5)
        
        // 2. Bayesian analysis - 10% weight
        val allBayesianResults = (1..PERIODONTAL_DISEASE_VECTORS.size).map { vectorId ->
            calculatePosteriorProbability(vectorId, featureMap)
        }.sortedByDescending { it.probability }
        val bayesianResults = allBayesianResults.take(5)
        
        // 3. Symbolic logic analysis - 40% weight
        val symbolicDiagnosis = applySymbolicLogicForPeriodontitis(teeth, patientInfo)
        val symbolicResults = if (symbolicDiagnosis != null) {
            listOf(symbolicDiagnosis.primaryDiagnosis!!.copy(symbolicMatch = true))
        } else {
            emptyList()
        }
        
        // 4. Integrate results with weights: distance 50%, bayesian 10%, symbolic 40%
        val weights = mapOf("distance" to 0.50, "bayesian" to 0.10, "symbolic" to 0.40)
        val integratedScores = mutableMapOf<String, Triple<Double, Double, Double>>() // (distance, bayesian, symbolic)
        
        // Add distance scores
        distanceResults.forEach { result ->
            val existing = integratedScores[result.name] ?: Triple(0.0, 0.0, 0.0)
            integratedScores[result.name] = Triple(
                existing.first + result.cosineSimilarity * weights["distance"]!!,
                existing.second,
                existing.third
            )
        }
        
        // Add bayesian scores
        bayesianResults.forEach { result ->
            val existing = integratedScores[result.name] ?: Triple(0.0, 0.0, 0.0)
            integratedScores[result.name] = Triple(
                existing.first,
                existing.second + result.probability * weights["bayesian"]!!,
                existing.third
            )
        }
        
        // Add symbolic scores
        symbolicResults.forEach { result ->
            val existing = integratedScores[result.name] ?: Triple(0.0, 0.0, 0.0)
            integratedScores[result.name] = Triple(
                existing.first,
                existing.second,
                existing.third + 1.0 * weights["symbolic"]!!
            )
        }
        
        // Get top integrated diagnosis
        val integratedDiagnosis = integratedScores.entries
            .maxByOrNull { it.value.first + it.value.second + it.value.third }
            ?.let { (name, scores) ->
                val distanceResult = distanceResults.firstOrNull { it.name == name }
                val bayesianResult = bayesianResults.firstOrNull { it.name == name }
                val symbolicResult = symbolicResults.firstOrNull { it.name == name }
                
                val finalDistance = if (scores.first > 0) scores.first / weights["distance"]!! else (distanceResult?.cosineSimilarity ?: 0.0)
                val finalBayesian = if (scores.second > 0) scores.second / weights["bayesian"]!! else (bayesianResult?.probability ?: 0.0)
                val finalSymbolic = scores.third > 0.5
                
                val baseResult = distanceResult ?: bayesianResult ?: symbolicResult
                baseResult?.copy(
                    probability = finalBayesian,
                    cosineSimilarity = finalDistance,
                    symbolicMatch = finalSymbolic
                )
            }
        
        return SeparatePeriodontalDiagnosisResults(
            featureVector = featureMap,
            distanceResults = distanceResults,
            bayesianResults = bayesianResults,
            symbolicResults = symbolicResults,
            integratedDiagnosis = integratedDiagnosis
        )
    }
    
    /**
     * Calculate cosine similarity between two vectors
     */
    private fun calculateCosineSimilarity(vecA: List<Double>, vecB: List<Double>): Double {
        if (vecA.size != vecB.size) return 0.0
        
        val dotProduct = vecA.zip(vecB).sumOf { it.first * it.second }
        val magnitudeA = kotlin.math.sqrt(vecA.sumOf { it * it })
        val magnitudeB = kotlin.math.sqrt(vecB.sumOf { it * it })
        
        return if (magnitudeA > 0.0 && magnitudeB > 0.0) {
            dotProduct / (magnitudeA * magnitudeB)
        } else {
            0.0
        }
    }
    
    /**
     * Get disease vector for cosine similarity calculation
     */
    private fun getDiseaseVectorForCosine(vectorId: Int): List<Double> {
        // Return expected feature values for each disease vector (13 features)
        // This is a simplified version - you may need to adjust based on your disease vectors
        return when (vectorId) {
            1 -> listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Healthy
            2 -> listOf(0.5, 0.5, 0.3, 0.2, 0.3, 0.3, 0.2, 0.2, 0.2, 0.2, 0.3, 0.0, 0.0) // Gingivitis
            3 -> listOf(0.4, 0.6, 0.8, 0.3, 0.4, 0.4, 0.3, 0.3, 0.3, 0.3, 0.4, 0.0, 0.0) // Gingivitis with Calculus
            4 -> listOf(0.3, 0.4, 0.5, 0.4, 0.5, 0.5, 0.4, 0.4, 0.4, 0.4, 0.5, 0.3, 0.2) // Localized Slight Periodontitis
            5 -> listOf(0.6, 0.7, 0.7, 0.6, 0.7, 0.7, 0.6, 0.6, 0.6, 0.6, 0.7, 0.5, 0.3) // Generalized Moderate Periodontitis
            6 -> listOf(0.8, 0.9, 0.9, 0.8, 0.9, 0.9, 0.8, 0.8, 0.8, 0.8, 0.9, 0.7, 0.5) // Severe Periodontitis
            7 -> listOf(0.4, 0.5, 0.6, 0.5, 0.6, 0.6, 0.5, 0.5, 0.5, 0.5, 0.6, 0.4, 0.3) // Aggressive Localized
            8 -> listOf(0.7, 0.8, 0.8, 0.7, 0.8, 0.8, 0.7, 0.7, 0.7, 0.7, 0.8, 0.6, 0.4) // Aggressive Generalized
            9 -> listOf(0.5, 0.6, 0.7, 0.6, 0.7, 0.7, 0.6, 0.6, 0.6, 0.6, 0.7, 0.5, 0.3) // Diabetes-associated
            10 -> listOf(0.9, 0.8, 0.7, 0.9, 0.9, 0.9, 0.8, 0.8, 0.8, 0.8, 0.9, 0.2, 0.1) // ANUG
            11 -> listOf(0.7, 0.6, 0.5, 0.8, 0.8, 0.8, 0.7, 0.7, 0.7, 0.7, 0.8, 0.1, 0.0) // Gingival Abscess
            12 -> listOf(0.8, 0.7, 0.6, 0.9, 0.9, 0.9, 0.8, 0.8, 0.8, 0.8, 0.9, 0.6, 0.4) // Periodontal Abscess
            13 -> listOf(0.3, 0.4, 0.5, 0.2, 0.3, 0.3, 0.2, 0.2, 0.2, 0.2, 0.3, 0.2, 0.8) // Gingival Recession
            14 -> listOf(0.4, 0.5, 0.4, 0.3, 0.6, 0.8, 0.5, 0.5, 0.5, 0.5, 0.4, 0.1, 0.1) // Drug-Induced Enlargement
            15 -> listOf(0.6, 0.5, 0.4, 0.5, 0.6, 0.6, 0.4, 0.4, 0.4, 0.4, 0.5, 0.0, 0.0) // Pregnancy Gingivitis
            else -> listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
    }
    
    /**
     * Apply symbolic logic to determine localized vs generalized periodontitis
     * Based on >30% of teeth affected = Generalized, ≤30% = Localized
     */
    private fun applySymbolicLogicForPeriodontitis(
        teeth: Map<Int, Tooth>,
        patientInfo: PatientInfo
    ): PeriodontalDiagnosis? {
        // Count teeth with periodontal disease indicators
        var teethWithPockets = 0
        var teethWithMobility = 0
        var teethWithRecession = 0
        var teethWithFurcation = 0
        var totalAffectedTeeth = 0
        val affectedToothNumbers = mutableSetOf<Int>()
        
        // Parse pocket depths and determine severity
        var slightCount = 0
        var moderateCount = 0
        var severeCount = 0
        
        teeth.values.forEach { tooth ->
            var isAffected = false
            
            // Check pocket depth
            if (tooth.pocket != "Normal 1-3mm" && !tooth.pocket.contains("1-3mm")) {
                teethWithPockets++
                isAffected = true
                affectedToothNumbers.add(tooth.number)
                
                // Determine severity from pocket depth
                when {
                    tooth.pocket.contains("8mm") || tooth.pocket.contains(">7mm") || tooth.pocket.contains(">8mm") || 
                    tooth.pocket.contains("9mm") || tooth.pocket.contains("10mm") || tooth.pocket.contains(">10mm") -> {
                        severeCount++
                    }
                    tooth.pocket.contains("6-7mm") || tooth.pocket.contains("6mm") || tooth.pocket.contains("7mm") -> {
                        moderateCount++
                    }
                    tooth.pocket.contains("4-5mm") || tooth.pocket.contains("4mm") || tooth.pocket.contains("5mm") -> {
                        slightCount++
                    }
                }
            }
            
            // Check mobility
            if (tooth.mobility != "None") {
                teethWithMobility++
                isAffected = true
                affectedToothNumbers.add(tooth.number)
            }
            
            // Check recession (significant recession indicates periodontitis)
            if (tooth.recession != "None" && !tooth.recession.contains("Minimal")) {
                teethWithRecession++
                isAffected = true
                affectedToothNumbers.add(tooth.number)
            }
            
            // Check furcation
            if (tooth.furcation != "None") {
                teethWithFurcation++
                isAffected = true
                affectedToothNumbers.add(tooth.number)
            }
            
            if (isAffected) {
                totalAffectedTeeth++
            }
        }
        
        val totalTeeth = teeth.size
        val affectedPercentage = (totalAffectedTeeth.toDouble() / totalTeeth) * 100.0
        
        // Determine if generalized (>30%) or localized (≤30%)
        val isGeneralized = affectedPercentage > 30.0
        
        // Determine severity
        val severity = when {
            severeCount > 0 || (moderateCount > 0 && teethWithFurcation > 0) -> "Severe"
            moderateCount > 0 || slightCount > 5 -> "Moderate"
            slightCount > 0 -> "Slight"
            else -> "Slight"
        }
        
        // Apply symbolic logic rules
        if (totalAffectedTeeth > 0) {
            // Determine ICD-11 codes based on localization and severity
            val icd11Primary = if (isGeneralized) {
                "DA0A.21" // Chronic Periodontitis - Generalized
            } else {
                "DA0A.20" // Chronic Periodontitis - Localized
            }
            
            val icd11Secondary = when (severity) {
                "Severe" -> "DA0A.24" // Severe
                "Moderate" -> "DA0A.23" // Moderate
                else -> "DA0A.22" // Slight
            }
            
            val diagnosisName = if (isGeneralized) {
                "Chronic Periodontitis - Generalized, $severity"
            } else {
                "Chronic Periodontitis - Localized, $severity"
            }
            
            return PeriodontalDiagnosis(
                segmentNumber = null,
                primaryDiagnosis = PeriodontalDiseaseVector(
                    id = if (isGeneralized) {
                        when (severity) {
                            "Severe" -> 6
                            "Moderate" -> 5
                            else -> 4
                        }
                    } else {
                        when (severity) {
                            "Severe" -> 6
                            "Moderate" -> 5
                            else -> 4
                        }
                    },
                    name = diagnosisName,
                    icd11Primary = icd11Primary,
                    icd11Secondary = icd11Secondary,
                    probability = 1.0,
                    matchedFeatures = listOf(
                        "Affected teeth: $totalAffectedTeeth/$totalTeeth (${formatDouble(affectedPercentage, 1)}%)",
                        "Teeth with pockets: $teethWithPockets",
                        "Teeth with mobility: $teethWithMobility",
                        "Teeth with recession: $teethWithRecession",
                        "Teeth with furcation: $teethWithFurcation"
                    )
                ),
                differentialDiagnoses = emptyList(),
                icd11Codes = listOf(icd11Primary, icd11Secondary)
            )
        }
        
        return null
    }
    
    // Helper function to format Double
    private fun formatDouble(value: Double, decimals: Int): String {
        return "%.${decimals}f".format(value)
    }
    
    /**
     * Diagnose a specific segment
     */
    fun diagnoseSegment(
        segment: GingivalSegment,
        patientInfo: PatientInfo,
        teeth: Map<Int, Tooth>
    ): PeriodontalDiagnosis {
        val features = extractSegmentFeatures(segment, patientInfo, teeth)
        return diagnoseFromFeatures(features, segment.number)
    }
    
    /**
     * Aggregate features from all segments for overall diagnosis
     */
    private fun aggregateSegmentFeatures(
        segments: Map<Int, GingivalSegment>,
        patientInfo: PatientInfo,
        teeth: Map<Int, Tooth>
    ): Map<Int, Double> {
        val aggregated = mutableMapOf<Int, Double>()
        
        // Count segments with abnormalities
        var segmentsWithBleeding = 0
        var segmentsWithPlaque = 0
        var segmentsWithCalculus = 0
        var segmentsWithExudate = 0
        var segmentsWithAbnormalColor = 0
        var segmentsWithAbnormalSize = 0
        
        segments.values.forEach { segment ->
            if (segment.bleeding != "No") segmentsWithBleeding++
            if (segment.plaque != "None") segmentsWithPlaque++
            if (segment.calculus != "None") segmentsWithCalculus++
            if (segment.exudate != "None") segmentsWithExudate++
            if (segment.color != "Pink") segmentsWithAbnormalColor++
            if (segment.sizeEdema != "Normal") segmentsWithAbnormalSize++
        }
        
        // Aggregate features
        aggregated[1] = segmentsWithBleeding.toDouble() / segments.size // Bleeding prevalence
        aggregated[2] = segmentsWithPlaque.toDouble() / segments.size // Plaque prevalence
        aggregated[3] = segmentsWithCalculus.toDouble() / segments.size // Calculus prevalence
        aggregated[4] = segmentsWithExudate.toDouble() / segments.size // Exudate prevalence
        aggregated[5] = segmentsWithAbnormalColor.toDouble() / segments.size // Color abnormality
        aggregated[6] = segmentsWithAbnormalSize.toDouble() / segments.size // Size abnormality
        
        // Count teeth with periodontal issues
        var teethWithPockets = 0
        var teethWithRecession = 0
        var teethWithMobility = 0
        var teethWithFurcation = 0
        
        teeth.values.forEach { tooth ->
            if (tooth.pocket != "Normal 1-3mm" && !tooth.pocket.contains("1-3mm")) {
                teethWithPockets++
            }
            if (tooth.recession != "None") {
                teethWithRecession++
            }
            if (tooth.mobility != "None") {
                teethWithMobility++
            }
            if (tooth.furcation != "None") {
                teethWithFurcation++
            }
        }
        
        aggregated[7] = teethWithPockets.toDouble() / teeth.size // Pocket prevalence
        aggregated[8] = teethWithRecession.toDouble() / teeth.size // Recession prevalence
        aggregated[9] = teethWithMobility.toDouble() / teeth.size // Mobility prevalence
        aggregated[10] = teethWithFurcation.toDouble() / teeth.size // Furcation prevalence
        
        // Patient factors
        aggregated[11] = parseSmokingStatus(patientInfo.smokingStatus)
        aggregated[12] = parseDiabeticStatus(patientInfo.diabeticStatus)
        aggregated[13] = parseOralHygiene(patientInfo.oralHygiene)
        
        return aggregated
    }
    
    /**
     * Extract features from a single segment
     */
    private fun extractSegmentFeatures(
        segment: GingivalSegment,
        patientInfo: PatientInfo,
        teeth: Map<Int, Tooth>
    ): Map<Int, Double> {
        val features = mutableMapOf<Int, Double>()
        
        // Gingival parameters
        features[1] = parseBleeding(segment.bleeding)
        features[2] = parsePlaque(segment.plaque)
        features[3] = parseCalculus(segment.calculus)
        features[4] = parseExudate(segment.exudate)
        features[5] = parseColor(segment.color)
        features[6] = parseSizeEdema(segment.sizeEdema)
        features[7] = parseTexture(segment.texture)
        features[8] = parseConsistency(segment.consistency)
        features[9] = parseShape(segment.shape)
        features[10] = parseContour(segment.contour)
        features[11] = parseDistribution(segment.distribution)
        
        // Get teeth in this segment (approximate mapping)
        val segmentTeeth = getTeethInSegment(segment.number, teeth)
        
        // Count periodontal issues in segment teeth
        var teethWithPockets = 0
        var teethWithRecession = 0
        var teethWithMobility = 0
        var teethWithFurcation = 0
        
        segmentTeeth.forEach { tooth ->
            if (tooth.pocket != "Normal 1-3mm" && !tooth.pocket.contains("1-3mm")) {
                teethWithPockets++
            }
            if (tooth.recession != "None") {
                teethWithRecession++
            }
            if (tooth.mobility != "None") {
                teethWithMobility++
            }
            if (tooth.furcation != "None") {
                teethWithFurcation++
            }
        }
        
        val segmentToothCount = segmentTeeth.size.toDouble()
        features[12] = if (segmentToothCount > 0) teethWithPockets / segmentToothCount else 0.0
        features[13] = if (segmentToothCount > 0) teethWithRecession / segmentToothCount else 0.0
        features[14] = if (segmentToothCount > 0) teethWithMobility / segmentToothCount else 0.0
        features[15] = if (segmentToothCount > 0) teethWithFurcation / segmentToothCount else 0.0
        
        // Patient factors
        features[16] = parseSmokingStatus(patientInfo.smokingStatus)
        features[17] = parseDiabeticStatus(patientInfo.diabeticStatus)
        features[18] = parseOralHygiene(patientInfo.oralHygiene)
        
        return features
    }
    
    /**
     * Get teeth numbers in a specific segment (sextant)
     */
    private fun getTeethInSegment(segmentNumber: Int, teeth: Map<Int, Tooth>): List<Tooth> {
        // Approximate mapping: segments 1-6 correspond to sextants
        val toothRanges = when (segmentNumber) {
            1 -> listOf(18, 17, 16, 15, 14, 13, 12, 11) // Upper right
            2 -> listOf(21, 22, 23, 24, 25, 26, 27, 28) // Upper left
            3 -> listOf(31, 32, 33, 34, 35, 36, 37, 38) // Lower left
            4 -> listOf(41, 42, 43, 44, 45, 46, 47, 48) // Lower right
            5 -> listOf(11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28) // Upper arch
            6 -> listOf(31, 32, 33, 34, 35, 36, 37, 38, 41, 42, 43, 44, 45, 46, 47, 48) // Lower arch
            else -> emptyList()
        }
        
        return toothRanges.mapNotNull { teeth[it] }
    }
    
    /**
     * Diagnose from extracted features
     */
    private fun diagnoseFromFeatures(
        features: Map<Int, Double>,
        segmentNumber: Int?
    ): PeriodontalDiagnosis {
        // Calculate posterior probabilities for all disease vectors
        val unnormalizedProbabilities = (1..PERIODONTAL_DISEASE_VECTORS.size).map { vectorId ->
            calculatePosteriorProbability(vectorId, features)
        }
        
        // Normalize probabilities
        val totalProbability = unnormalizedProbabilities.sumOf { it.probability }
        val normalizedProbabilities = if (totalProbability > 0) {
            unnormalizedProbabilities.map { vector ->
                vector.copy(probability = vector.probability / totalProbability)
            }
        } else {
            unnormalizedProbabilities
        }
        
        // Apply symbolic logic rules
        val ruleBoostedProbabilities = normalizedProbabilities.map { vector ->
            val ruleMatch = checkSymbolicRules(vector.id, features)
            if (ruleMatch) {
                vector.copy(probability = minOf(1.0, vector.probability * 1.5))
            } else {
                vector
            }
        }
        
        // Re-normalize after rule boosting
        val totalAfterBoost = ruleBoostedProbabilities.sumOf { it.probability }
        val finalProbabilities = if (totalAfterBoost > 0) {
            ruleBoostedProbabilities.map { vector ->
                vector.copy(probability = vector.probability / totalAfterBoost)
            }
        } else {
            ruleBoostedProbabilities
        }
        
        // Sort by probability
        val sortedDiagnoses = finalProbabilities
            .sortedByDescending { it.probability }
            .filter { it.probability > 0.01 }
        
        val primary = sortedDiagnoses.firstOrNull()
        val differentials = sortedDiagnoses.drop(1).take(5)
        
        // Collect ICD-11 codes
        val icd11Codes = mutableListOf<String>()
        primary?.let {
            icd11Codes.add(it.icd11Primary)
            if (it.icd11Secondary.isNotEmpty()) {
                icd11Codes.add(it.icd11Secondary)
            }
        }
        
        return PeriodontalDiagnosis(
            segmentNumber = segmentNumber,
            primaryDiagnosis = primary,
            differentialDiagnoses = differentials,
            icd11Codes = icd11Codes.distinct()
        )
    }
    
    /**
     * Calculate posterior probability using Bayes' theorem
     */
    private fun calculatePosteriorProbability(
        vectorId: Int,
        features: Map<Int, Double>
    ): PeriodontalDiseaseVector {
        val prior = PERIODONTAL_PRIOR_PROBABILITIES[vectorId] ?: 0.0
        val vectorInfo = PERIODONTAL_DISEASE_VECTORS[vectorId] ?: return PeriodontalDiseaseVector(
            id = vectorId,
            name = "Unknown",
            icd11Primary = "",
            probability = 0.0,
            matchedFeatures = emptyList()
        )
        
        // Calculate likelihood: P(Features | Disease)
        var likelihood = 1.0
        val matchedFeatures = mutableListOf<String>()
        
        features.forEach { (featureId, value) ->
            val featureLikelihood = getLikelihood(vectorId, featureId, value)
            likelihood *= featureLikelihood
            
            if (featureLikelihood > 0.5) {
                matchedFeatures.add("Feature $featureId: ${getFeatureName(featureId)}")
            }
        }
        
        val unnormalizedPosterior = likelihood * prior
        
        return PeriodontalDiseaseVector(
            id = vectorId,
            name = vectorInfo.name,
            icd11Primary = vectorInfo.icd11Primary,
            icd11Secondary = vectorInfo.icd11Secondary,
            probability = unnormalizedPosterior,
            matchedFeatures = matchedFeatures,
            cosineSimilarity = 0.0,
            symbolicMatch = false
        )
    }
    
    /**
     * Get likelihood for a specific feature value given a disease vector
     */
    private fun getLikelihood(vectorId: Int, featureId: Int, value: Double): Double {
        return when (vectorId) {
            // V1: Healthy Periodontium - only if ALL features are normal
            1 -> when (featureId) {
                1 -> if (value == 0.0) 0.98 else 0.01 // No bleeding
                2 -> if (value == 0.0) 0.95 else 0.01 // No plaque
                3 -> if (value == 0.0) 0.95 else 0.01 // No calculus
                4 -> if (value == 0.0) 0.98 else 0.01 // No exudate
                5 -> if (value == 0.0) 0.95 else 0.01 // Normal color (Pink)
                6 -> if (value == 0.0) 0.95 else 0.01 // Normal size
                12 -> if (value == 0.0) 0.90 else 0.01 // No pockets
                13 -> if (value == 0.0) 0.90 else 0.01 // No recession
                else -> 0.85 // Reduced default likelihood
            }
            
            // V2: Plaque-Induced Gingivitis (DA0A.10)
            2 -> when (featureId) {
                1 -> if (value > 0.0) 0.80 else 0.20 // Bleeding present
                2 -> if (value > 0.0) 0.90 else 0.10 // Plaque present
                3 -> if (value == 0.0) 0.70 else 0.30 // Usually no calculus
                4 -> if (value == 0.0) 0.85 else 0.15 // Usually no exudate
                5 -> if (value == 1.0) 0.70 else 0.30 // Reddish-pink
                12 -> if (value == 0.0) 0.95 else 0.05 // No pockets
                13 -> if (value == 0.0) 0.90 else 0.10 // No recession
                else -> 0.70
            }
            
            // V3: Chronic Gingivitis with Calculus (DA0A.10)
            3 -> when (featureId) {
                1 -> if (value > 0.0) 0.75 else 0.25
                2 -> if (value > 0.0) 0.85 else 0.15
                3 -> if (value > 0.0) 0.90 else 0.10 // Calculus present
                5 -> if (value == 1.0) 0.65 else 0.35
                12 -> if (value == 0.0) 0.90 else 0.10
                else -> 0.65
            }
            
            // V4: Chronic Periodontitis - Localized, Slight (DA0A.20, DA0A.22)
            4 -> when (featureId) {
                1 -> if (value > 0.0) 0.80 else 0.20
                2 -> if (value > 0.0) 0.85 else 0.15
                3 -> if (value > 0.0) 0.80 else 0.20
                12 -> if (value > 0.0 && value < 0.3) 0.75 else 0.25 // Some pockets
                13 -> if (value > 0.0 && value < 0.2) 0.60 else 0.40 // Some recession
                14 -> if (value == 0.0) 0.85 else 0.15 // Usually no mobility
                else -> 0.60
            }
            
            // V5: Chronic Periodontitis - Generalized, Moderate (DA0A.21, DA0A.23)
            5 -> when (featureId) {
                1 -> if (value > 0.3) 0.85 else 0.15 // Multiple segments bleeding
                2 -> if (value > 0.3) 0.90 else 0.10
                3 -> if (value > 0.3) 0.85 else 0.15
                12 -> if (value > 0.2 && value < 0.5) 0.80 else 0.20 // Moderate pockets
                13 -> if (value > 0.1 && value < 0.4) 0.70 else 0.30
                14 -> if (value > 0.0 && value < 0.2) 0.60 else 0.40 // Some mobility
                else -> 0.65
            }
            
            // V6: Chronic Periodontitis - Severe (DA0A.24)
            6 -> when (featureId) {
                1 -> if (value > 0.4) 0.85 else 0.15
                2 -> if (value > 0.4) 0.90 else 0.10
                3 -> if (value > 0.4) 0.85 else 0.15
                12 -> if (value > 0.3) 0.85 else 0.15 // Many deep pockets
                13 -> if (value > 0.3) 0.80 else 0.20 // Significant recession
                14 -> if (value > 0.1) 0.75 else 0.25 // Mobility present
                15 -> if (value > 0.0) 0.70 else 0.30 // Furcation involvement
                else -> 0.70
            }
            
            // V7: Aggressive Periodontitis - Localized (DA0A.30)
            7 -> when (featureId) {
                1 -> if (value > 0.0 && value < 0.3) 0.80 else 0.20 // Limited segments
                2 -> if (value > 0.0) 0.70 else 0.30 // May have minimal plaque
                12 -> if (value > 0.0 && value < 0.3) 0.85 else 0.15 // Localized deep pockets
                13 -> if (value > 0.0 && value < 0.3) 0.75 else 0.25
                14 -> if (value > 0.0) 0.70 else 0.30
                16 -> if (value == 0.0) 0.90 else 0.10 // Usually non-smoker
                else -> 0.60
            }
            
            // V8: Aggressive Periodontitis - Generalized (DA0A.31)
            8 -> when (featureId) {
                1 -> if (value > 0.4) 0.85 else 0.15
                2 -> if (value > 0.0) 0.60 else 0.40 // Disproportionate to plaque
                12 -> if (value > 0.3) 0.90 else 0.10 // Widespread deep pockets
                13 -> if (value > 0.2) 0.80 else 0.20
                14 -> if (value > 0.1) 0.75 else 0.25
                15 -> if (value > 0.0) 0.70 else 0.30
                16 -> if (value == 0.0) 0.85 else 0.15
                else -> 0.70
            }
            
            // V9: Periodontitis with Systemic Disease - Diabetes (DA0A.4)
            9 -> when (featureId) {
                1 -> if (value > 0.3) 0.80 else 0.20
                12 -> if (value > 0.2) 0.85 else 0.15
                17 -> if (value == 1.0) 0.90 else 0.10 // Diabetic
                else -> 0.65
            }
            
            // V10: Necrotizing Ulcerative Gingivitis (DA0A.00)
            10 -> when (featureId) {
                1 -> if (value > 0.0) 0.95 else 0.05 // Severe bleeding
                4 -> if (value > 0.0) 0.90 else 0.10 // Exudate present
                5 -> if (value == 2.0) 0.85 else 0.15 // Red
                6 -> if (value == 1.0) 0.80 else 0.20 // Enlarged
                7 -> if (value == 2.0) 0.85 else 0.15 // Loss of stippling
                8 -> if (value == 1.0) 0.80 else 0.20 // Spongy
                12 -> if (value == 0.0) 0.95 else 0.05 // No pockets
                else -> 0.50
            }
            
            // V11: Gingival Abscess (DA0A.60)
            11 -> when (featureId) {
                1 -> if (value > 0.0) 0.85 else 0.15
                4 -> if (value > 0.0) 0.90 else 0.10 // Exudate
                5 -> if (value == 1.0 || value == 2.0) 0.80 else 0.20
                6 -> if (value == 1.0) 0.85 else 0.15 // Localized enlargement
                12 -> if (value == 0.0) 0.90 else 0.10 // Usually no pockets
                else -> 0.60
            }
            
            // V12: Periodontal Abscess (DA0A.61)
            12 -> when (featureId) {
                1 -> if (value > 0.0) 0.90 else 0.10
                4 -> if (value > 0.0) 0.95 else 0.05 // Exudate
                12 -> if (value > 0.0) 0.90 else 0.10 // Pockets present
                13 -> if (value > 0.0) 0.70 else 0.30
                else -> 0.65
            }
            
            // V13: Gingival Recession (DA0A.82)
            13 -> when (featureId) {
                1 -> if (value == 0.0) 0.85 else 0.15 // Usually no bleeding
                2 -> if (value == 0.0) 0.70 else 0.30
                13 -> if (value > 0.0) 0.95 else 0.05 // Recession present
                12 -> if (value == 0.0) 0.80 else 0.20 // Usually no pockets
                else -> 0.70
            }
            
            // V14: Drug-Induced Gingival Enlargement (DA0A.13)
            14 -> when (featureId) {
                1 -> if (value == 0.0) 0.70 else 0.30
                6 -> if (value == 1.0 || value == 2.0) 0.90 else 0.10 // Enlargement
                5 -> if (value == 0.0) 0.80 else 0.20 // Usually pink
                12 -> if (value == 0.0) 0.85 else 0.15
                else -> 0.60
            }
            
            // V15: Pregnancy-Associated Gingivitis (DA0A.12)
            15 -> when (featureId) {
                1 -> if (value > 0.0) 0.85 else 0.15
                2 -> if (value > 0.0) 0.80 else 0.20
                5 -> if (value == 1.0) 0.75 else 0.25 // Reddish-pink
                6 -> if (value == 1.0) 0.70 else 0.30 // Enlarged
                12 -> if (value == 0.0) 0.90 else 0.10
                else -> 0.65
            }
            
            else -> 0.50
        }
    }
    
    /**
     * Check symbolic logic rules
     */
    private fun checkSymbolicRules(vectorId: Int, features: Map<Int, Double>): Boolean {
        val bleeding = features[1] ?: 0.0
        val plaque = features[2] ?: 0.0
        val calculus = features[3] ?: 0.0
        val exudate = features[4] ?: 0.0
        val color = features[5] ?: 0.0
        val size = features[6] ?: 0.0
        val pockets = features[12] ?: 0.0
        val recession = features[13] ?: 0.0
        val mobility = features[14] ?: 0.0
        val furcation = features[15] ?: 0.0
        val smoking = features[16] ?: 0.0
        val diabetes = features[17] ?: 0.0
        
        return when (vectorId) {
            // V1: Healthy
            1 -> bleeding == 0.0 && plaque == 0.0 && calculus == 0.0 && 
                 exudate == 0.0 && pockets == 0.0
            
            // V2: Plaque-Induced Gingivitis
            2 -> bleeding > 0.0 && plaque > 0.0 && pockets == 0.0
            
            // V3: Chronic Gingivitis with Calculus
            3 -> bleeding > 0.0 && plaque > 0.0 && calculus > 0.0 && pockets == 0.0
            
            // V4: Chronic Periodontitis - Localized, Slight
            4 -> pockets > 0.0 && pockets < 0.3 && recession < 0.2 && mobility == 0.0
            
            // V5: Chronic Periodontitis - Generalized, Moderate
            5 -> pockets > 0.2 && pockets < 0.5 && recession > 0.1 && recession < 0.4
            
            // V6: Chronic Periodontitis - Severe
            6 -> pockets > 0.3 && recession > 0.3 && (mobility > 0.1 || furcation > 0.0)
            
            // V7: Aggressive Periodontitis - Localized
            7 -> pockets > 0.0 && pockets < 0.3 && plaque < 0.3 && smoking == 0.0
            
            // V8: Aggressive Periodontitis - Generalized
            8 -> pockets > 0.3 && plaque < 0.5 && smoking == 0.0
            
            // V9: Periodontitis with Diabetes
            9 -> pockets > 0.2 && diabetes == 1.0
            
            // V10: ANUG
            10 -> bleeding > 0.0 && exudate > 0.0 && color == 2.0 && pockets == 0.0
            
            // V11: Gingival Abscess
            11 -> exudate > 0.0 && size == 1.0 && pockets == 0.0
            
            // V12: Periodontal Abscess
            12 -> exudate > 0.0 && pockets > 0.0
            
            // V13: Gingival Recession
            13 -> recession > 0.0 && pockets == 0.0 && bleeding == 0.0
            
            // V14: Drug-Induced Enlargement
            14 -> size >= 1.0 && color == 0.0 && pockets == 0.0
            
            // V15: Pregnancy Gingivitis
            15 -> bleeding > 0.0 && pockets == 0.0 && size == 1.0
            
            else -> false
        }
    }
    
    // Parsing functions
    private fun parseBleeding(value: String): Double {
        return when {
            value.contains("Yes") || value.contains("yes") || value != "No" -> 1.0
            else -> 0.0
        }
    }
    
    private fun parsePlaque(value: String): Double {
        return when {
            value.contains("None") || value.contains("none") -> 0.0
            value.contains("Mild") || value.contains("mild") -> 0.3
            value.contains("Moderate") || value.contains("moderate") -> 0.6
            value.contains("Heavy") || value.contains("heavy") || value.contains("Severe") -> 1.0
            else -> 0.5
        }
    }
    
    private fun parseCalculus(value: String): Double {
        return when {
            value.contains("None") || value.contains("none") -> 0.0
            value.contains("Supragingival") -> 0.5
            value.contains("Subgingival") -> 0.8
            value.contains("Both") -> 1.0
            else -> 0.0
        }
    }
    
    private fun parseExudate(value: String): Double {
        return when {
            value.contains("None") || value.contains("none") -> 0.0
            value.contains("Slight") || value.contains("slight") -> 0.3
            value.contains("Moderate") || value.contains("moderate") -> 0.6
            value.contains("Heavy") || value.contains("heavy") -> 1.0
            else -> 0.0
        }
    }
    
    private fun parseColor(value: String): Double {
        return when {
            value.contains("Pink") || value.contains("pink") -> 0.0
            value.contains("Reddish-pink") || value.contains("reddish") -> 1.0
            value.contains("Red") || value.contains("red") -> 2.0
            value.contains("Dark red") || value.contains("cyanotic") -> 3.0
            else -> 0.0
        }
    }
    
    private fun parseSizeEdema(value: String): Double {
        return when {
            value.contains("Normal") || value.contains("normal") -> 0.0
            value.contains("Recession") || value.contains("recession") -> -1.0
            value.contains("Mild") || value.contains("mild") -> 1.0
            value.contains("Moderate") || value.contains("moderate") -> 2.0
            value.contains("Marked") || value.contains("marked") -> 3.0
            else -> 0.0
        }
    }
    
    private fun parseTexture(value: String): Double {
        return when {
            value.contains("Stippled") || value.contains("normal") -> 0.0
            value.contains("Loss of stippling") -> 1.0
            value.contains("Smooth") || value.contains("shiny") -> 2.0
            value.contains("Fibrotic") || value.contains("leathery") -> 3.0
            value.contains("Ulcerated") -> 4.0
            else -> 0.0
        }
    }
    
    private fun parseConsistency(value: String): Double {
        return when {
            value.contains("Firm") || value.contains("resilient") -> 0.0
            value.contains("Spongy") -> 1.0
            value.contains("Soft") || value.contains("edematous") -> 2.0
            value.contains("Hyperkeratotic") -> 3.0
            value.contains("Friable") -> 4.0
            value.contains("Retractile") -> 5.0
            else -> 0.0
        }
    }
    
    private fun parseShape(value: String): Double {
        return when {
            value.contains("Normal") || value.contains("scalloped") -> 0.0
            value.contains("Bulbous") -> 1.0
            value.contains("Cleft") -> 2.0
            value.contains("Cratered") -> 3.0
            else -> 0.0
        }
    }
    
    private fun parseContour(value: String): Double {
        return when {
            value.contains("Normal") || value.contains("pointed") -> 0.0
            value.contains("Blunted") -> 1.0
            value.contains("Rolled") -> 2.0
            value.contains("Flattened") -> 3.0
            else -> 0.0
        }
    }
    
    private fun parseDistribution(value: String): Double {
        return when {
            value.contains("None") -> 0.0
            value.contains("Localized") -> 1.0
            value.contains("Generalized") -> 2.0
            else -> 0.0
        }
    }
    
    private fun parseSmokingStatus(value: String): Double {
        return when {
            value.contains("Never") || value.contains("never") -> 0.0
            value.contains("Former") || value.contains("former") -> 0.5
            value.contains("Light") || value.contains("light") -> 0.7
            value.contains("Heavy") || value.contains("heavy") -> 1.0
            else -> 0.0
        }
    }
    
    private fun parseDiabeticStatus(value: String): Double {
        return when {
            value.contains("Normal") || value.contains("normal") -> 0.0
            value.contains("Prediabetic") -> 0.5
            value.contains("Controlled") -> 0.7
            value.contains("Poorly controlled") -> 1.0
            else -> 0.0
        }
    }
    
    private fun parseOralHygiene(value: String): Double {
        return when {
            value.contains("Good") || value.contains("good") -> 0.0
            value.contains("Fair") || value.contains("fair") -> 0.5
            value.contains("Poor") || value.contains("poor") -> 1.0
            else -> 0.0
        }
    }
    
    private fun getFeatureName(featureId: Int): String {
        return when (featureId) {
            1 -> "Bleeding"
            2 -> "Plaque"
            3 -> "Calculus"
            4 -> "Exudate"
            5 -> "Color"
            6 -> "Size/Edema"
            7 -> "Texture"
            8 -> "Consistency"
            9 -> "Shape"
            10 -> "Contour"
            11 -> "Distribution"
            12 -> "Pocket Depth"
            13 -> "Recession"
            14 -> "Mobility"
            15 -> "Furcation"
            16 -> "Smoking Status"
            17 -> "Diabetic Status"
            18 -> "Oral Hygiene"
            else -> "Unknown"
        }
    }
    
    companion object {
        // Prior probabilities for periodontal disease vectors
        // Reduced healthy prior from 30% to 10% to allow better detection of abnormalities
        val PERIODONTAL_PRIOR_PROBABILITIES = mapOf(
            1 to 0.100,  // Healthy Periodontium (reduced from 0.300)
            2 to 0.250,  // Plaque-Induced Gingivitis
            3 to 0.150,  // Chronic Gingivitis with Calculus
            4 to 0.120,  // Chronic Periodontitis - Localized, Slight (increased from 0.100)
            5 to 0.100,  // Chronic Periodontitis - Generalized, Moderate (increased from 0.080)
            6 to 0.070,  // Chronic Periodontitis - Severe (increased from 0.050)
            7 to 0.030,  // Aggressive Periodontitis - Localized (increased from 0.020)
            8 to 0.025,  // Aggressive Periodontitis - Generalized (increased from 0.015)
            9 to 0.030,  // Periodontitis with Systemic Disease (increased from 0.020)
            10 to 0.010, // Necrotizing Ulcerative Gingivitis (increased from 0.005)
            11 to 0.015, // Gingival Abscess (increased from 0.010)
            12 to 0.020, // Periodontal Abscess (increased from 0.015)
            13 to 0.060, // Gingival Recession (increased from 0.050)
            14 to 0.015, // Drug-Induced Gingival Enlargement (increased from 0.010)
            15 to 0.025  // Pregnancy-Associated Gingivitis (increased from 0.020)
        )
        
        // Periodontal disease vector information
        val PERIODONTAL_DISEASE_VECTORS = mapOf(
            1 to PeriodontalDiseaseInfo("Healthy Periodontium", "None", ""),
            2 to PeriodontalDiseaseInfo("Plaque-Induced Gingivitis", "DA0A.10", ""),
            3 to PeriodontalDiseaseInfo("Chronic Gingivitis with Calculus", "DA0A.10", ""),
            4 to PeriodontalDiseaseInfo("Chronic Periodontitis - Localized, Slight", "DA0A.20", "DA0A.22"),
            5 to PeriodontalDiseaseInfo("Chronic Periodontitis - Generalized, Moderate", "DA0A.21", "DA0A.23"),
            6 to PeriodontalDiseaseInfo("Chronic Periodontitis - Severe", "DA0A.21", "DA0A.24"),
            7 to PeriodontalDiseaseInfo("Aggressive Periodontitis - Localized", "DA0A.30", ""),
            8 to PeriodontalDiseaseInfo("Aggressive Periodontitis - Generalized", "DA0A.31", ""),
            9 to PeriodontalDiseaseInfo("Periodontitis Associated with Diabetes", "DA0A.40", ""),
            10 to PeriodontalDiseaseInfo("Acute Necrotizing Ulcerative Gingivitis (ANUG)", "DA0A.00", ""),
            11 to PeriodontalDiseaseInfo("Gingival Abscess", "DA0A.60", ""),
            12 to PeriodontalDiseaseInfo("Periodontal Abscess", "DA0A.61", ""),
            13 to PeriodontalDiseaseInfo("Gingival Recession", "DA0A.82", ""),
            14 to PeriodontalDiseaseInfo("Drug-Induced Gingival Enlargement", "DA0A.13", ""),
            15 to PeriodontalDiseaseInfo("Pregnancy-Associated Gingivitis", "DA0A.12", "")
        )
        
        data class PeriodontalDiseaseInfo(
            val name: String,
            val icd11Primary: String,
            val icd11Secondary: String
        )
    }
}

