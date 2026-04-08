package com.ram.orai.oraic

/**
 * Complete Tooth Fuzzy Diagnostic System
 * Based on Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
 */

data class ToothDiagnosis(
    val toothNumber: Int,
    val primaryDiagnosis: DiseaseVector?,
    val differentialDiagnoses: List<DiseaseVector>,
    val icd11Codes: List<String>
)

data class SeparateDiagnosisResults(
    val featureVector: List<Double>,              // Actual feature vector values
    val distanceResults: List<DiseaseVector>,      // Cosine similarity results (top 5)
    val bayesianResults: List<DiseaseVector>,       // Bayesian probability results (top 5)
    val symbolicResults: List<DiseaseVector>,      // Symbolic logic results (top 5)
    val integratedDiagnosis: DiseaseVector?       // Final integrated diagnosis
)

data class IntegratedDiseaseVector(
    val name: String,
    val icd11Primary: String,
    val icd11Secondary: String,
    val integratedScore: Double,                 // Weighted combined score
    val distanceScore: Double,                    // Cosine similarity score
    val bayesianScore: Double,                    // Bayesian probability score
    val symbolicScore: Double                     // Symbolic logic score (1.0 if matched, 0.0 otherwise)
)

data class DiseaseVector(
    val id: Int,
    val name: String,
    val icd11Primary: String,
    val icd11Secondary: String = "",
    val probability: Double,
    val cosineSimilarity: Double = 0.0,
    val symbolicMatch: Boolean = false
)

class ToothDiagnosticEngine {
    
    /**
     * Calculate diagnosis using Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
     */
    fun diagnoseTooth(tooth: Tooth, patientInfo: PatientInfo): ToothDiagnosis {
        // Extract feature vector (21 features)
        val featureVector = extractFeatureVector(tooth, patientInfo)
        
        // Step 1: Apply Symbolic Logic Rules (highest priority)
        val symbolicDiagnosis = applySymbolicLogic(featureVector, tooth.number)
        if (symbolicDiagnosis != null) {
            return ToothDiagnosis(
                toothNumber = tooth.number,
                primaryDiagnosis = symbolicDiagnosis,
                differentialDiagnoses = emptyList(),
                icd11Codes = listOf(symbolicDiagnosis.icd11Primary).filter { it.isNotEmpty() }
            )
        }
        
        // Step 2: Calculate Vector Cosine Similarity for all disease vectors
        val cosineSimilarities = (1..39).map { vectorId -> // Updated to include all vectors
            val diseaseVector = getDiseaseVector(vectorId)
            val similarity = calculateCosineSimilarity(featureVector, diseaseVector)
            Pair(vectorId, similarity)
        }.sortedByDescending { it.second }
        
        // Step 3: Apply Bayesian Inference to top matches
        val topCandidates = cosineSimilarities.take(10) // Top 10 by cosine similarity
        val bayesianProbabilities = topCandidates.map { (vectorId, cosineSim) ->
            val prior = PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val likelihood = cosineSim // Use cosine similarity as likelihood
            val posterior = (likelihood * prior) / calculateNormalizationConstant(featureVector)
            
            val vectorInfo = DISEASE_VECTORS[vectorId] ?: DiseaseInfo("Unknown", "", "")
            DiseaseVector(
                id = vectorId,
                name = vectorInfo.name,
                icd11Primary = vectorInfo.icd11Primary,
                icd11Secondary = vectorInfo.icd11Secondary,
                probability = posterior,
                cosineSimilarity = cosineSim,
                symbolicMatch = false
            )
        }.sortedByDescending { it.probability }
        
        val primary = topCandidates.firstOrNull()?.let { (vectorId, _) ->
            bayesianProbabilities.firstOrNull { it.id == vectorId }
        }
        val differentials = bayesianProbabilities.filter { it.id != primary?.id }.take(5)
        
        return ToothDiagnosis(
            toothNumber = tooth.number,
            primaryDiagnosis = primary,
            differentialDiagnoses = differentials,
            icd11Codes = listOfNotNull(primary?.icd11Primary).filter { it.isNotEmpty() }
        )
    }
    
    /**
     * Get separate diagnosis results for each method and integrated diagnosis
     */
    fun getSeparateDiagnosisResults(tooth: Tooth, patientInfo: PatientInfo): SeparateDiagnosisResults {
        val featureVector = extractFeatureVector(tooth, patientInfo)
        
        // 1. Distance-based analysis (Cosine Similarity) - 50% weight
        val distanceResults = (1..39).map { vectorId ->
            val diseaseVector = getDiseaseVector(vectorId)
            val similarity = calculateCosineSimilarity(featureVector, diseaseVector)
            val vectorInfo = DISEASE_VECTORS[vectorId] ?: DiseaseInfo("Unknown", "", "")
            DiseaseVector(
                id = vectorId,
                name = vectorInfo.name,
                icd11Primary = vectorInfo.icd11Primary,
                icd11Secondary = vectorInfo.icd11Secondary,
                probability = 0.0,
                cosineSimilarity = similarity,
                symbolicMatch = false
            )
        }.sortedByDescending { it.cosineSimilarity }.take(5)
        
        // 2. Bayesian analysis - 10% weight
        val allBayesianResults = (1..39).map { vectorId ->
            val prior = PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val diseaseVector = getDiseaseVector(vectorId)
            val cosineSim = calculateCosineSimilarity(featureVector, diseaseVector)
            val likelihood = cosineSim
            val posterior = (likelihood * prior) / calculateNormalizationConstant(featureVector)
            val vectorInfo = DISEASE_VECTORS[vectorId] ?: DiseaseInfo("Unknown", "", "")
            DiseaseVector(
                id = vectorId,
                name = vectorInfo.name,
                icd11Primary = vectorInfo.icd11Primary,
                icd11Secondary = vectorInfo.icd11Secondary,
                probability = posterior,
                cosineSimilarity = cosineSim,
                symbolicMatch = false
            )
        }.sortedByDescending { it.probability }
        val bayesianResults = allBayesianResults.take(5)
        
        // 3. Symbolic logic analysis - 40% weight
        val symbolicDiagnosis = applySymbolicLogic(featureVector, tooth.number)
        val symbolicResults = if (symbolicDiagnosis != null) {
            listOf(symbolicDiagnosis)
        } else {
            emptyList()
        }
        
        // 4. Integrate results with weights: distance 50%, bayesian 10%, symbolic 40%
        val weights = mapOf("distance" to 0.50, "bayesian" to 0.10, "symbolic" to 0.40)
        val integratedScores = mutableMapOf<String, IntegratedDiseaseVector>()
        
        // Add distance scores
        distanceResults.forEach { result ->
            val existing = integratedScores[result.name]
            integratedScores[result.name] = IntegratedDiseaseVector(
                name = result.name,
                icd11Primary = result.icd11Primary,
                icd11Secondary = result.icd11Secondary,
                integratedScore = (existing?.integratedScore ?: 0.0) + result.cosineSimilarity * weights["distance"]!!,
                distanceScore = result.cosineSimilarity,
                bayesianScore = existing?.bayesianScore ?: 0.0,
                symbolicScore = existing?.symbolicScore ?: 0.0
            )
        }
        
        // Add bayesian scores
        bayesianResults.forEach { result ->
            val existing = integratedScores[result.name]
            integratedScores[result.name] = IntegratedDiseaseVector(
                name = result.name,
                icd11Primary = result.icd11Primary,
                icd11Secondary = result.icd11Secondary,
                integratedScore = (existing?.integratedScore ?: 0.0) + result.probability * weights["bayesian"]!!,
                distanceScore = existing?.distanceScore ?: 0.0,
                bayesianScore = result.probability,
                symbolicScore = existing?.symbolicScore ?: 0.0
            )
        }
        
        // Add symbolic scores
        symbolicResults.forEach { result ->
            val existing = integratedScores[result.name]
            integratedScores[result.name] = IntegratedDiseaseVector(
                name = result.name,
                icd11Primary = result.icd11Primary,
                icd11Secondary = result.icd11Secondary,
                integratedScore = (existing?.integratedScore ?: 0.0) + 1.0 * weights["symbolic"]!!,
                distanceScore = existing?.distanceScore ?: 0.0,
                bayesianScore = existing?.bayesianScore ?: 0.0,
                symbolicScore = 1.0
            )
        }
        
        // Get top integrated diagnosis
        val integratedDiagnosis = integratedScores.values
            .sortedByDescending { it.integratedScore }
            .firstOrNull()
            ?.let { integrated ->
                DiseaseVector(
                    id = 0,
                    name = integrated.name,
                    icd11Primary = integrated.icd11Primary,
                    icd11Secondary = integrated.icd11Secondary,
                    probability = integrated.bayesianScore,
                    cosineSimilarity = integrated.distanceScore,
                    symbolicMatch = integrated.symbolicScore > 0.5
                )
            }
        
        return SeparateDiagnosisResults(
            featureVector = featureVector,
            distanceResults = distanceResults,
            bayesianResults = bayesianResults,
            symbolicResults = symbolicResults,
            integratedDiagnosis = integratedDiagnosis
        )
    }
    
    /**
     * Extract 21-feature vector from tooth and patientInfo
     */
    private fun extractFeatureVector(tooth: Tooth, patientInfo: PatientInfo): List<Double> {
        return listOf(
            tooth.number.toDouble(), // Feature 1: Tooth Number
            parseNumberStatus(patientInfo.numberStatus), // Feature 2
            parseToothSize(tooth.toothSize), // Feature 3
            parseToothShape(tooth.toothShape), // Feature 4
            parseToothMorphology(tooth.toothMorphology), // Feature 5
            parseMatrixFormation(patientInfo.matrixFormation), // Feature 6
            parseMineralization(patientInfo.mineralization), // Feature 7
            0.0, // Feature 8: Root Formation (not available)
            parseEruptionStatus(tooth.eruptionStatus), // Feature 9
            0.0, // Feature 10: Not used
            parseCaries(tooth.caries), // Feature 11: Caries
            parseErosion(tooth.erosion), // Feature 12
            parseFractureType(tooth.fractureType), // Feature 13
            parseAttrition(tooth.attrition), // Feature 14
            parseAbrasion(tooth.abrasion), // Feature 15
            parseAbfraction(tooth.abfraction), // Feature 16
            parseDiscoloration(tooth.discoloration), // Feature 17
            parsePainStatus(tooth.painStatus), // Feature 18
            parseTenderPercussion(tooth.tenderOnPercussion), // Feature 19
            parseVestibularTenderness(tooth.vestibularTenderness), // Feature 20
            parseSinusTract(tooth.sinusTract) // Feature 21
        )
    }
    
    /**
     * Apply Symbolic Logic Rules (highest priority - textbook rules)
     */
    private fun applySymbolicLogic(features: List<Double>, toothNumber: Int): DiseaseVector? {
        val caries = features[10] // Index 10 = Feature 11
        val fracture = features[12] // Index 12 = Feature 13
        val pain = features[17] // Index 17 = Feature 18
        val tender = features[18] // Index 18 = Feature 19
        val vestibular = features[19] // Index 19 = Feature 20
        val sinus = features[20] // Index 20 = Feature 21
        val toothSize = features[2] // Index 2 = Feature 3
        val toothShape = features[3] // Index 3 = Feature 4
        val toothMorphology = features[4] // Index 4 = Feature 5
        val eruption = features[8] // Index 8 = Feature 9
        
        // Rule 1: Caries takes priority over developmental anomalies
        when {
            caries >= 6.0 && pain == 5.0 && tender == 1.0 && sinus == 1.0 -> {
                return createDiseaseVector(10, "Chronic Periapical Abscess with Sinus", "DA09.2", "")
            }
            caries >= 6.0 && pain == 4.0 && tender == 1.0 && vestibular == 1.0 -> {
                return createDiseaseVector(9, "Acute Periapical Abscess", "DA09.3", "")
            }
            caries >= 6.0 && pain == 5.0 && tender == 0.0 -> {
                return createDiseaseVector(8, "Necrotic Pulp - Asymptomatic Apical Periodontitis", "DA09.1", "")
            }
            caries >= 3.0 && pain == 3.0 && tender == 0.0 -> {
                return createDiseaseVector(7, "Irreversible Pulpitis", "DA09.0", "")
            }
            caries >= 2.0 && pain == 1.0 && tender == 0.0 -> {
                return createDiseaseVector(6, "Reversible Pulpitis", "DA09.0", "")
            }
            caries == 4.0 && pain == 0.0 -> {
                return createDiseaseVector(5, "Arrested Caries", "DA08.3", "")
            }
            caries >= 5.0 && pain >= 3.0 -> {
                return createDiseaseVector(4, "Severe Dentin Caries", "DA08.1", "DA09.0")
            }
            caries == 3.0 -> {
                return createDiseaseVector(3, "Dentin Caries - Moderate", "DA08.1", "")
            }
            caries == 2.0 -> {
                return createDiseaseVector(3, "Dentin Caries - Moderate", "DA08.1", "")
            }
            caries == 1.0 -> {
                return createDiseaseVector(2, "Enamel Caries - Incipient", "DA08.0", "")
            }
        }
        
        // Rule 2: Fractures take priority (if no caries)
        if (caries == 0.0) {
            when {
                fracture == 6.0 && pain == 3.0 && tender == 1.0 && sinus >= 1.0 -> {
                    return createDiseaseVector(16, "Vertical Root Fracture", "DA0C.2", "")
                }
                fracture == 5.0 && pain == 2.0 && tender == 1.0 -> {
                    return createDiseaseVector(15, "Horizontal Root Fracture", "DA0C.2", "")
                }
                fracture == 3.0 && pain >= 2.0 -> {
                    return createDiseaseVector(14, "Complicated Crown Fracture (Ellis Class III)", "DA0C.1", "")
                }
                fracture == 2.0 && pain == 1.0 -> {
                    return createDiseaseVector(13, "Dentin Fracture (Ellis Class II)", "DA0C.0", "")
                }
                fracture == 1.0 && pain == 0.0 -> {
                    return createDiseaseVector(12, "Enamel Fracture (Ellis Class I)", "DA0C.0", "")
                }
            }
        }
        
        // Rule 3: Developmental anomalies (only if no caries and no fractures)
        if (caries == 0.0 && fracture == 0.0) {
            when {
                // Tooth Morphology anomalies (checked first as they're more specific)
                toothMorphology == 4.0 -> {
                    return createDiseaseVector(34, "Talon Cusp", "LA71.Y", "")
                }
                toothMorphology == 3.0 -> {
                    return createDiseaseVector(35, "Lobodontia/Globodontia", "LA71.Y", "")
                }
                toothMorphology == 2.0 -> {
                    return createDiseaseVector(36, "Taurodontism", "LA71.Y", "")
                }
                toothMorphology == 1.0 -> {
                    return createDiseaseVector(37, "Dens Evaginatus", "LA71.5", "")
                }
                toothMorphology == -1.0 -> {
                    return createDiseaseVector(26, "Dens Invaginatus Type I", "LA71.4", "")
                }
                toothMorphology == -2.0 -> {
                    return createDiseaseVector(26, "Dens Invaginatus Type II", "LA71.4", "")
                }
                toothMorphology == -3.0 -> {
                    return createDiseaseVector(26, "Dens Invaginatus Type III", "LA71.4", "")
                }
                toothMorphology == -4.0 -> {
                    return createDiseaseVector(38, "Dilaceration", "LA71.Y", "")
                }
                // Tooth Size anomalies
                toothSize > 0.0 -> {
                    return createDiseaseVector(31, "Macrodontia", "LA71.0", "")
                }
                toothSize <= -4.0 -> {
                    return createDiseaseVector(24, "Microdontia - Lateral Incisor", "LA71.1", "")
                }
                // Tooth Shape anomalies
                toothShape == 2.0 -> {
                    return createDiseaseVector(32, "Twinning", "LA71.Y", "")
                }
                toothShape == 1.0 -> {
                    return createDiseaseVector(33, "Gemination", "LA71.3", "")
                }
                toothShape == -1.0 -> {
                    return createDiseaseVector(25, "Fusion - Primary Teeth", "LA71.2", "")
                }
                toothShape == -2.0 -> {
                    return createDiseaseVector(39, "Concrescence", "LA71.Y", "")
                }
                // Eruption anomalies
                eruption == -4.0 && (toothNumber == 18 || toothNumber == 28 || toothNumber == 38 || toothNumber == 48) -> {
                    return createDiseaseVector(22, "Impacted Third Molar", "LA70.0", "")
                }
                eruption == -5.0 && (toothNumber == 13 || toothNumber == 23 || toothNumber == 33 || toothNumber == 43) -> {
                    return createDiseaseVector(23, "Ectopic Canine", "LA70.1", "")
                }
            }
        }
        
        // Rule 4: Tooth wear (only if no caries, no fractures, no developmental anomalies)
        if (caries == 0.0 && fracture == 0.0 && toothSize == 0.0 && toothShape == 0.0 && toothMorphology == 0.0) {
            val erosion = features[11] // Index 11 = Feature 12
            val attrition = features[13] // Index 13 = Feature 14
            val abrasion = features[14] // Index 14 = Feature 15
            val abfraction = features[15] // Index 15 = Feature 16
            
            when {
                erosion >= 2.0 -> {
                    return createDiseaseVector(17, "Dental Erosion - Dietary", "DA0B.0", "")
                }
                attrition >= 2.0 -> {
                    return createDiseaseVector(19, "Attrition - Moderate", "DA0B.1", "")
                }
                abrasion >= 2.0 -> {
                    return createDiseaseVector(20, "Abrasion - Toothbrush", "DA0B.2", "")
                }
                abfraction >= 2.0 -> {
                    return createDiseaseVector(21, "Abfraction - Stress-Related", "DA0B.3", "")
                }
            }
        }
        
        // Rule 5: Healthy tooth (only if ALL features are normal)
        if (caries == 0.0 && fracture == 0.0 && toothSize == 0.0 && toothShape == 0.0 && toothMorphology == 0.0 &&
            features[11] == 0.0 && features[12] == 0.0 && features[13] == 0.0 &&
            features[14] == 0.0 && features[15] == 0.0 && pain == 0.0 && tender == 0.0) {
            return createDiseaseVector(1, "Healthy Tooth", "None", "")
        }
        
        return null // No symbolic match, use cosine similarity + Bayesian
    }
    
    /**
     * Calculate Cosine Similarity between feature vector and disease vector
     */
    private fun calculateCosineSimilarity(
        featureVector: List<Double>,
        diseaseVector: List<Double>
    ): Double {
        if (featureVector.size != diseaseVector.size) return 0.0
        
        val dotProduct = featureVector.zip(diseaseVector).sumOf { (f, d) -> f * d }
        val featureMagnitude = Math.sqrt(featureVector.sumOf { it * it })
        val diseaseMagnitude = Math.sqrt(diseaseVector.sumOf { it * it })
        
        if (featureMagnitude == 0.0 || diseaseMagnitude == 0.0) return 0.0
        
        return dotProduct / (featureMagnitude * diseaseMagnitude)
    }
    
    /**
     * Get disease vector template (expected feature values for each disease)
     */
    private fun getDiseaseVector(vectorId: Int): List<Double> {
        // Return expected feature vector for each disease type
        // This is a simplified template - in practice, these would be learned from data
        return when (vectorId) {
            1 -> listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Healthy
            2 -> listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0) // Enamel Caries
            3 -> listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0) // Dentin Caries Moderate
            31 -> listOf(0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Macrodontia
            32 -> listOf(0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Twinning
            33 -> listOf(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Gemination
            34 -> listOf(0.0, 0.0, 0.0, 0.0, 4.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Talon Cusp
            35 -> listOf(0.0, 0.0, 0.0, 0.0, 3.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Lobodontia
            36 -> listOf(0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Taurodontism
            37 -> listOf(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Dens Evaginatus
            38 -> listOf(0.0, 0.0, 0.0, 0.0, -4.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Dilaceration
            39 -> listOf(0.0, 0.0, 0.0, -2.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Concrescence
            else -> List(21) { 0.0 } // Default: all zeros
        }
    }
    
    /**
     * Calculate normalization constant for Bayesian inference
     */
    private fun calculateNormalizationConstant(featureVector: List<Double>): Double {
        var sum = 0.0
        for (vectorId in 1..39) { // Updated to include all vectors
            val prior = PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val diseaseVector = getDiseaseVector(vectorId)
            val likelihood = calculateCosineSimilarity(featureVector, diseaseVector)
            sum += likelihood * prior
        }
        return if (sum > 0.0) sum else 1.0
    }
    
    private fun createDiseaseVector(id: Int, name: String, icd11Primary: String, icd11Secondary: String): DiseaseVector {
        val vectorInfo = DISEASE_VECTORS[id] ?: DiseaseInfo(name, icd11Primary, icd11Secondary)
        return DiseaseVector(
            id = id,
            name = vectorInfo.name,
            icd11Primary = vectorInfo.icd11Primary,
            icd11Secondary = vectorInfo.icd11Secondary,
            probability = 1.0, // Symbolic match = 100% confidence
            cosineSimilarity = 1.0,
            symbolicMatch = true
        )
    }
    
    // Parsing functions (same as before)
    private fun parseNumberStatus(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Multiple supernumerary") -> 3.0
            value.contains("+2") || value.contains("Two supernumerary") -> 2.0
            value.contains("+1") || value.contains("Single supernumerary") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Single missing") -> -1.0
            value.contains("-2") || value.contains("Multiple missing") -> -2.0
            value.contains("-3") || value.contains("Severe hypodontia") -> -3.0
            value.contains("-4") || value.contains("Anodontia") -> -4.0
            else -> 0.0
        }
    }
    
    private fun parseToothSize(value: String): Double {
        return when {
            value.contains("Macrodontia", ignoreCase = true) -> {
                val match = Regex("""\+(\d+)""").find(value)
                match?.groupValues?.get(1)?.toDoubleOrNull() ?: 5.0
            }
            value.contains("Microdontia", ignoreCase = true) -> {
                val match = Regex("""-(\d+)""").find(value)
                -(match?.groupValues?.get(1)?.toDoubleOrNull() ?: 5.0)
            }
            value.contains("Normal", ignoreCase = true) || value == "0 = Normal (±1.5mm)" -> 0.0
            value.startsWith("+") -> {
                val match = Regex("""\+(\d+)""").find(value)
                match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            }
            value.startsWith("-") -> {
                val match = Regex("""-(\d+)""").find(value)
                -(match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0)
            }
            else -> {
                val match = Regex("""([+-]?\d+)""").find(value)
                match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
            }
        }
    }
    
    private fun parseToothShape(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Twinning") -> 2.0
            value.contains("+1") || value.contains("Gemination") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Fusion") -> -1.0
            value.contains("-2") || value.contains("Concrescence") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseToothMorphology(value: String): Double {
        return when {
            value.contains("+4") || value.contains("Talon cusp") -> 4.0
            value.contains("+3") || value.contains("Lobodontia") -> 3.0
            value.contains("+2") || value.contains("Taurodontism") -> 2.0
            value.contains("+1") || value.contains("Dens evaginatus") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Dens invaginatus Type I") -> -1.0
            value.contains("-2") || value.contains("Dens invaginatus Type II") -> -2.0
            value.contains("-3") || value.contains("Dens invaginatus Type III") -> -3.0
            value.contains("-4") || value.contains("Dilaceration") -> -4.0
            else -> 0.0
        }
    }
    
    private fun parseMatrixFormation(value: String): Double {
        return when {
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Mild hypoplasia") -> -1.0
            value.contains("-2") || value.contains("Moderate hypoplasia") -> -2.0
            value.contains("-3") || value.contains("Severe hypoplasia") -> -3.0
            value.contains("-4") || value.contains("Amelogenesis Imperfecta") -> -4.0
            value.contains("-5") || value.contains("Dentinogenesis Imperfecta") -> -5.0
            value.contains("-6") || value.contains("Dentin Dysplasia") -> -6.0
            value.contains("-7") || value.contains("Regional Odontodysplasia") -> -7.0
            value.contains("-8") || value.contains("Environmental") -> -8.0
            else -> 0.0
        }
    }
    
    private fun parseMineralization(value: String): Double {
        val match = Regex("""([\d.]+)""").find(value)
        val num = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.7
        return when {
            num > 1.5 -> 1.6
            num >= 1.3 -> 1.4
            num >= 1.1 -> 1.2
            num >= 1.0 -> 1.05
            num >= 0.7 -> 0.85
            num >= 0.5 -> 0.6
            num >= 0.4 -> 0.45
            num >= 0.2 -> 0.3
            else -> 0.1
        }
    }
    
    private fun parseEruptionStatus(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Natal") -> 2.0
            value.contains("+1") || value.contains("Neonatal") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Submerged") -> -1.0
            value.contains("-2") || value.contains("Retained") -> -2.0
            value.contains("-3") || value.contains("Embedded") -> -3.0
            value.contains("-4") || value.contains("Impacted") -> -4.0
            value.contains("-5") || value.contains("Ectopic") -> -5.0
            else -> 0.0
        }
    }
    
    private fun parseCaries(value: String): Double {
        return when {
            value.contains("+1") || value.contains("ICDAS 1") -> 1.0
            value.contains("+2") || value.contains("ICDAS 2") -> 2.0
            value.contains("+3") || value.contains("ICDAS 3") -> 3.0
            value.contains("+4") || value.contains("ICDAS 4") -> 4.0
            value.contains("+5") || value.contains("ICDAS 5") -> 5.0
            value.contains("+6") || value.contains("ICDAS 6") -> 6.0
            value.contains("+7") || value.contains("Root caries - Active") -> 7.0
            value.contains("+8") || value.contains("Root caries - Cavitated") -> 8.0
            value.contains("0") || value.contains("Sound") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseErosion(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Initial") -> 1.0
            value.contains("+2") || value.contains("Defect") && value.contains("<50%") -> 2.0
            value.contains("+3") || value.contains("≥50%") -> 3.0
            value.contains("0") || value.contains("No erosive") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseFractureType(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Ellis Class I") -> 1.0
            value.contains("+2") || value.contains("Ellis Class II") -> 2.0
            value.contains("+3") || value.contains("Ellis Class III") -> 3.0
            value.contains("+4") || value.contains("Crown-root") -> 4.0
            value.contains("+5") || value.contains("Horizontal root") -> 5.0
            value.contains("+6") || value.contains("Vertical root") -> 6.0
            value.contains("+7") || value.contains("Oblique root") -> 7.0
            value.contains("+8") || value.contains("Comminuted") -> 8.0
            value.contains("0") || value.contains("No fracture") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseAttrition(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Minimal") -> 1.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+4") || value.contains("Extreme") -> 4.0
            value.contains("0") || value.contains("No attrition") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseAbrasion(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Minimal") -> 1.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+4") || value.contains("Extreme") -> 4.0
            value.contains("0") || value.contains("No abrasion") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseAbfraction(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Minimal") -> 1.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+4") || value.contains("Extreme") -> 4.0
            value.contains("0") || value.contains("No abfraction") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseDiscoloration(value: String): Double {
        return when {
            value.contains("+1") || value.contains("White spot") -> 1.0
            value.contains("+2") || value.contains("Yellow") -> 2.0
            value.contains("+3") || value.contains("Brown") -> 3.0
            value.contains("+4") || value.contains("Gray") -> 4.0
            value.contains("+5") || value.contains("Black") -> 5.0
            value.contains("+6") || value.contains("Pink") -> 6.0
            value.contains("+7") || value.contains("Tetracycline") -> 7.0
            value.contains("+8") || value.contains("Amelogenesis") -> 8.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parsePainStatus(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Cold and hot") -> 1.0
            value.contains("+2") || value.contains("Stimuli only") -> 2.0
            value.contains("+3") || value.contains("Prolonged") || value.contains("Spontaneous") -> 3.0
            value.contains("+4") || value.contains("Severe") -> 4.0
            value.contains("+5") || value.contains("No response") || value.contains("Necrotic") -> 5.0
            value.contains("0") || value.contains("No pain") || value.contains("asymptomatic") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseTenderPercussion(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Positive") -> 1.0
            value.contains("0") || value.contains("Negative") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseVestibularTenderness(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Positive") -> 1.0
            value.contains("0") || value.contains("Negative") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseSinusTract(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Multiple") -> 2.0
            value.contains("+1") || value.contains("Opened") || value.contains("draining") -> 1.0
            value.contains("0") || value.contains("Normal") || value.contains("no sinus") -> 0.0
            else -> 0.0
        }
    }
    
    companion object {
        // Prior probabilities (textbook-based)
        val PRIOR_PROBABILITIES = mapOf(
            1 to 0.100,  // Healthy Tooth
            2 to 0.150,  // Enamel Caries - Incipient
            3 to 0.120,  // Dentin Caries - Moderate
            4 to 0.080,  // Severe Dentin Caries
            5 to 0.030,  // Arrested Caries
            6 to 0.040,  // Reversible Pulpitis
            7 to 0.035,  // Irreversible Pulpitis
            8 to 0.025,  // Necrotic Pulp - Asymptomatic
            9 to 0.015,  // Acute Periapical Abscess
            10 to 0.010, // Chronic Periapical Abscess with Sinus
            11 to 0.005, // Radicular Cyst
            12 to 0.020, // Enamel Fracture
            13 to 0.015, // Dentin Fracture
            14 to 0.008, // Complicated Crown Fracture
            15 to 0.005, // Horizontal Root Fracture
            16 to 0.003, // Vertical Root Fracture
            17 to 0.025, // Dental Erosion - Dietary
            18 to 0.010, // Dental Erosion - Intrinsic
            19 to 0.020, // Attrition - Moderate
            20 to 0.015, // Abrasion - Toothbrush
            21 to 0.012, // Abfraction
            22 to 0.050, // Impacted Third Molar
            23 to 0.015, // Ectopic Canine
            24 to 0.020, // Microdontia
            25 to 0.005, // Fusion
            26 to 0.010, // Dens Invaginatus
            27 to 0.002, // Amelogenesis Imperfecta
            28 to 0.001, // Dentinogenesis Imperfecta
            29 to 0.025, // MIH
            30 to 0.030, // Fluorosis
            31 to 0.030, // Macrodontia
            32 to 0.030, // Twinning
            33 to 0.030, // Gemination
            34 to 0.020, // Talon Cusp
            35 to 0.010, // Lobodontia/Globodontia
            36 to 0.015, // Taurodontism
            37 to 0.015, // Dens Evaginatus
            38 to 0.010, // Dilaceration
            39 to 0.005  // Concrescence
        )
        
        val DISEASE_VECTORS = mapOf(
            1 to DiseaseInfo("Healthy Tooth", "None", ""),
            2 to DiseaseInfo("Enamel Caries - Incipient", "DA08.0", ""),
            3 to DiseaseInfo("Dentin Caries - Moderate", "DA08.1", ""),
            4 to DiseaseInfo("Severe Dentin Caries", "DA08.1", "DA09.0"),
            5 to DiseaseInfo("Arrested Caries", "DA08.3", ""),
            6 to DiseaseInfo("Reversible Pulpitis", "DA09.0", ""),
            7 to DiseaseInfo("Irreversible Pulpitis", "DA09.0", ""),
            8 to DiseaseInfo("Necrotic Pulp - Asymptomatic Apical Periodontitis", "DA09.1", ""),
            9 to DiseaseInfo("Acute Periapical Abscess", "DA09.3", ""),
            10 to DiseaseInfo("Chronic Periapical Abscess with Sinus", "DA09.2", ""),
            11 to DiseaseInfo("Radicular Cyst", "DA09.4", ""),
            12 to DiseaseInfo("Enamel Fracture (Ellis Class I)", "DA0C.0", ""),
            13 to DiseaseInfo("Dentin Fracture (Ellis Class II)", "DA0C.0", ""),
            14 to DiseaseInfo("Complicated Crown Fracture (Ellis Class III)", "DA0C.1", ""),
            15 to DiseaseInfo("Horizontal Root Fracture", "DA0C.2", ""),
            16 to DiseaseInfo("Vertical Root Fracture", "DA0C.2", ""),
            17 to DiseaseInfo("Dental Erosion - Dietary", "DA0B.0", ""),
            18 to DiseaseInfo("Dental Erosion - Intrinsic (GERD)", "DA0B.0", ""),
            19 to DiseaseInfo("Attrition - Moderate", "DA0B.1", ""),
            20 to DiseaseInfo("Abrasion - Toothbrush", "DA0B.2", ""),
            21 to DiseaseInfo("Abfraction - Stress-Related", "DA0B.3", ""),
            22 to DiseaseInfo("Impacted Third Molar", "LA70.0", ""),
            23 to DiseaseInfo("Ectopic Canine", "LA70.1", ""),
            24 to DiseaseInfo("Microdontia - Lateral Incisor", "LA71.1", ""),
            25 to DiseaseInfo("Fusion - Primary Teeth", "LA71.2", ""),
            26 to DiseaseInfo("Dens Invaginatus", "LA71.4", ""),
            27 to DiseaseInfo("Amelogenesis Imperfecta", "LA72.0", ""),
            28 to DiseaseInfo("Dentinogenesis Imperfecta", "LA72.1", ""),
            29 to DiseaseInfo("Molar Incisor Hypomineralization (MIH)", "LA72.5", ""),
            30 to DiseaseInfo("Fluorosis - Moderate", "5B50.0", ""),
            31 to DiseaseInfo("Macrodontia", "LA71.0", ""),
            32 to DiseaseInfo("Twinning", "LA71.Y", ""),
            33 to DiseaseInfo("Gemination", "LA71.3", ""),
            34 to DiseaseInfo("Talon Cusp", "LA71.Y", ""),
            35 to DiseaseInfo("Lobodontia/Globodontia", "LA71.Y", ""),
            36 to DiseaseInfo("Taurodontism", "LA71.6", ""),
            37 to DiseaseInfo("Dens Evaginatus", "LA71.5", ""),
            38 to DiseaseInfo("Dilaceration", "LA71.Y", ""),
            39 to DiseaseInfo("Concrescence", "LA71.Y", "")
        )
        
        data class DiseaseInfo(
            val name: String,
            val icd11Primary: String,
            val icd11Secondary: String
        )
    }
}
