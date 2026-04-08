package com.ram.orai.oraic

/**
 * Complete Malocclusion Fuzzy Diagnostic System
 * Based on Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
 */

data class MalocclusionDiagnosis(
    val primaryDiagnosis: MalocclusionDiseaseVector?,
    val differentialDiagnoses: List<MalocclusionDiseaseVector>,
    val icd11Codes: List<String>
)

data class MalocclusionDiseaseVector(
    val id: Int,
    val name: String,
    val icd11Primary: String,
    val icd11Secondary: String = "",
    val probability: Double,
    val cosineSimilarity: Double = 0.0,
    val symbolicMatch: Boolean = false
)

class MalocclusionDiagnosticEngine {
    
    /**
     * Calculate diagnosis using Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
     */
    fun diagnose(patientInfo: PatientInfo): MalocclusionDiagnosis {
        // Extract feature vector from patientInfo
        val featureVector = extractFeatureVector(patientInfo)
        
        // Step 1: Apply Symbolic Logic Rules (highest priority)
        val symbolicDiagnosis = applySymbolicLogic(featureVector)
        if (symbolicDiagnosis != null) {
            return MalocclusionDiagnosis(
                primaryDiagnosis = symbolicDiagnosis,
                differentialDiagnoses = emptyList(),
                icd11Codes = listOf(symbolicDiagnosis.icd11Primary).filter { it.isNotEmpty() }
            )
        }
        
        // Step 2: Calculate Vector Cosine Similarity for all disease vectors
        val cosineSimilarities = (1..MALOCCLUSION_DISEASE_VECTORS.size).map { vectorId ->
            val diseaseVector = getDiseaseVector(vectorId)
            val similarity = calculateCosineSimilarity(featureVector, diseaseVector)
            Pair(vectorId, similarity)
        }.sortedByDescending { it.second }
        
        // Step 3: Apply Bayesian Inference to top matches
        val topCandidates = cosineSimilarities.take(10) // Top 10 by cosine similarity
        val bayesianProbabilities = topCandidates.map { (vectorId, cosineSim) ->
            val prior = MALOCCLUSION_PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val likelihood = cosineSim // Use cosine similarity as likelihood
            val posterior = (likelihood * prior) / calculateNormalizationConstant(featureVector)
            
            val vectorInfo = MALOCCLUSION_DISEASE_VECTORS[vectorId] ?: MalocclusionDiseaseInfo("Unknown", "", "")
            MalocclusionDiseaseVector(
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
        
        return MalocclusionDiagnosis(
            primaryDiagnosis = primary,
            differentialDiagnoses = differentials,
            icd11Codes = listOfNotNull(primary?.icd11Primary).filter { it.isNotEmpty() }
        )
    }
    
    /**
     * Extract feature vector from PatientInfo (56 parameters)
     */
    private fun extractFeatureVector(patientInfo: PatientInfo): List<Double> {
        return listOf(
            // Extra-oral Examination Parameters (20 features)
            parseShapeOfHead(patientInfo.shapeOfHead),
            parseFacialSymmetry(patientInfo.facialSymmetry),
            parseFacialForm(patientInfo.facialForm),
            parseFacialDivergence(patientInfo.facialDivergence),
            parseInterlabialGap(patientInfo.interlabialGapAtRest),
            parseNasolabialAngle(patientInfo.nasolabialAngle),
            parseUpperLipPosture(patientInfo.upperLipPosture),
            parseLowerLipPosture(patientInfo.lowerLipPosture),
            parseLipTonicity(patientInfo.lipTonicity),
            parseRestingLowerLipLine(patientInfo.restingLowerLipLine),
            parseMentolabialSulcus(patientInfo.mentolabialSulcus),
            parseChinPosition(patientInfo.chinPosition),
            parseApSkeletalRelationship(patientInfo.apSkeletalRelationship),
            parseTotalFaceHeight(patientInfo.totalFaceHeight),
            parseUpperFaceHeight(patientInfo.upperFaceHeight),
            parseMiddleFaceHeight(patientInfo.middleFaceHeight),
            parseLowerFaceHeight(patientInfo.lowerFaceHeight),
            parseVto(patientInfo.vto),
            parseClinicalFmpa(patientInfo.clinicalFmpa),
            parseIncisorExposureAtRest(patientInfo.incisorExposureAtRest),
            parseIncisorExposureAtSpeech(patientInfo.incisorExposureAtSpeech),
            parseIncisorExposureOnSmiling(patientInfo.incisorExposureOnSmiling),
            // Functional Examination Parameters (8 features)
            parseRespiratoryPattern(patientInfo.respiratoryPattern),
            parseDeglutition(patientInfo.deglutition),
            parseMastication(patientInfo.mastication),
            parseSpeech(patientInfo.speech),
            parsePathOfClosure(patientInfo.pathOfClosure),
            parseLateralPathOfClosure(patientInfo.lateralPathOfClosure),
            parseTmjFunction(patientInfo.tmjFunction),
            parsePosturalRestPositionOfTongue(patientInfo.posturalRestPositionOfTongue),
            // Maxillary Arch Parameters (7 features)
            parseMaxillaryArchShape(patientInfo.maxillaryArchShape),
            parseMaxillaryArchSymmetry(patientInfo.maxillaryArchSymmetry),
            parseMaxillaryCrowding(patientInfo.maxillaryCrowding),
            parseMaxillarySpacing(patientInfo.maxillarySpacing),
            parseMaxillaryRotation(patientInfo.maxillaryRotation),
            parseMaxillaryAxialInclination(patientInfo.maxillaryAxialInclination),
            parseMaxillaryTransposition(patientInfo.maxillaryTransposition),
            // Mandibular Arch Parameters (7 features)
            parseMandibularArchShape(patientInfo.mandibularArchShape),
            parseMandibularArchSymmetry(patientInfo.mandibularArchSymmetry),
            parseMandibularCrowding(patientInfo.mandibularCrowding),
            parseMandibularSpacing(patientInfo.mandibularSpacing),
            parseMandibularRotation(patientInfo.mandibularRotation),
            parseMandibularAxialInclination(patientInfo.mandibularAxialInclination),
            parseMandibularTransposition(patientInfo.mandibularTransposition),
            // Inter-Arch Relationship Parameters (12 features)
            parseMaximumMouthOpening(patientInfo.maximumMouthOpening),
            parseFreewaySpace(patientInfo.freewaySpace),
            parseCurveOfSpee(patientInfo.curveOfSpee),
            parseMolarRelationship(patientInfo.molarRelationship),
            parseCanineRelationship(patientInfo.canineRelationship),
            parseIncisorRelationship(patientInfo.incisorRelationship),
            parseOverjet(patientInfo.overjet),
            parseOverbite(patientInfo.overbite),
            parseCrossbiteType(patientInfo.crossbiteType),
            parseUpperDentalMidline(patientInfo.upperDentalMidline),
            parseLowerDentalMidline(patientInfo.lowerDentalMidline),
            parseInterArchMidlineDiscrepancy(patientInfo.interArchMidlineDiscrepancy)
        )
    }
    
    /**
     * Apply Symbolic Logic Rules (highest priority - textbook rules)
     */
    private fun applySymbolicLogic(features: List<Double>): MalocclusionDiseaseVector? {
        val molarRel = features[45] // Index 45 = Molar Relationship
        val canineRel = features[46] // Index 46 = Canine Relationship
        val incisorRel = features[47] // Index 47 = Incisor Relationship
        val overjet = features[48] // Index 48 = Overjet
        val overbite = features[49] // Index 49 = Overbite
        val crossbite = features[50] // Index 50 = Crossbite Type
        val maxCrowding = features[34] // Index 34 = Maxillary Crowding
        val mandCrowding = features[41] // Index 41 = Mandibular Crowding
        val maxSpacing = features[35] // Index 35 = Maxillary Spacing
        val mandSpacing = features[42] // Index 42 = Mandibular Spacing
        val apSkeletal = features[12] // Index 12 = AP Skeletal Relationship
        val tmjFunc = features[29] // Index 29 = TMJ Function
        val respiratory = features[23] // Index 23 = Respiratory Pattern
        val deglutition = features[24] // Index 24 = Deglutition
        
        // Rule 1: Angle's Classification (Molar Relationship)
        when {
            molarRel <= -3.0 -> {
                return createDiseaseVector(2, "Malocclusion, Angle Class II Division 1", "DA0C.21", "")
            }
            molarRel == -4.0 -> {
                return createDiseaseVector(3, "Malocclusion, Angle Class II Division 2", "DA0C.21", "")
            }
            molarRel >= 3.0 -> {
                return createDiseaseVector(4, "Malocclusion, Angle Class III", "DA0C.22", "")
            }
            molarRel == 0.0 && canineRel == 0.0 && incisorRel == 0.0 -> {
                return createDiseaseVector(1, "Malocclusion, Angle Class I", "DA0C.20", "")
            }
        }
        
        // Rule 2: Crowding
        if ((maxCrowding < 0.0 && Math.abs(maxCrowding) >= 2.0) || 
            (mandCrowding < 0.0 && Math.abs(mandCrowding) >= 2.0)) {
            return createDiseaseVector(5, "Crowding of Teeth", "DA0C.24", "")
        }
        
        // Rule 3: Spacing
        if ((maxSpacing > 0.0 && maxSpacing >= 2.0) || 
            (mandSpacing > 0.0 && mandSpacing >= 2.0)) {
            return createDiseaseVector(6, "Excessive Spacing of Teeth", "DA0C.23", "")
        }
        
        // Rule 4: Crossbite (Reverse Articulation)
        if (crossbite > 0.0) {
            return createDiseaseVector(7, "Reverse Articulation (Crossbite)", "DA0C.26", "")
        }
        
        // Rule 5: Skeletal Anomalies
        when {
            apSkeletal <= -2.0 -> {
                return createDiseaseVector(8, "Retrognathism (Mandibular)", "DA0C.12", "")
            }
            apSkeletal >= 2.0 -> {
                return createDiseaseVector(9, "Prognathism (Mandibular)", "DA0C.11", "")
            }
        }
        
        // Rule 6: Functional Abnormalities
        when {
            respiratory >= 2.0 -> {
                return createDiseaseVector(10, "Malocclusion due to Mouth Breathing", "DA0C.52", "")
            }
            deglutition >= 2.0 -> {
                return createDiseaseVector(11, "Malocclusion due to Abnormal Swallowing", "DA0C.51", "")
            }
            tmjFunc >= 2.0 -> {
                return createDiseaseVector(12, "Temporomandibular Joint-Pain-Dysfunction Syndrome", "DA0C.60", "")
            }
            tmjFunc == 1.0 -> {
                return createDiseaseVector(13, "Clicking Jaw", "DA0C.61", "")
            }
        }
        
        // Rule 7: Open Bite / Deep Bite
        when {
            overbite >= 4.0 -> {
                return createDiseaseVector(14, "Severe Open Bite", "DA0C.2Y", "")
            }
            overbite <= -5.0 -> {
                return createDiseaseVector(15, "Severe Deep Bite", "DA0C.2Y", "")
            }
        }
        
        // Rule 8: Normal occlusion (only if ALL features are normal)
        if (molarRel == 0.0 && canineRel == 0.0 && incisorRel == 0.0 &&
            overjet == 0.0 && overbite == 0.0 && crossbite == 0.0 &&
            maxCrowding == 0.0 && mandCrowding == 0.0 &&
            maxSpacing == 0.0 && mandSpacing == 0.0 &&
            apSkeletal == 0.0 && tmjFunc == 0.0 && respiratory == 0.0) {
            return createDiseaseVector(0, "Normal Occlusion", "None", "")
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
        return when (vectorId) {
            0 -> List(56) { 0.0 } // Normal Occlusion
            1 -> {
                // Class I - all relationships normal
                List(56) { 0.0 }.toMutableList().apply {
                    this[45] = 0.0 // Molar relationship = Class I
                    this[46] = 0.0 // Canine relationship = Class I
                    this[47] = 0.0 // Incisor relationship = Class I
                }
            }
            2 -> {
                // Class II Division 1
                List(56) { 0.0 }.toMutableList().apply {
                    this[45] = -3.0 // Molar relationship = Class II Div 1
                    this[48] = -2.0 // Increased overjet
                }
            }
            3 -> {
                // Class II Division 2
                List(56) { 0.0 }.toMutableList().apply {
                    this[45] = -4.0 // Molar relationship = Class II Div 2
                    this[49] = -2.0 // Deep bite
                }
            }
            4 -> {
                // Class III
                List(56) { 0.0 }.toMutableList().apply {
                    this[45] = 3.0 // Molar relationship = Class III
                    this[48] = 2.0 // Reverse overjet
                }
            }
            5 -> {
                // Crowding
                List(56) { 0.0 }.toMutableList().apply {
                    this[34] = -2.0 // Maxillary crowding
                    this[41] = -2.0 // Mandibular crowding
                }
            }
            6 -> {
                // Spacing
                List(56) { 0.0 }.toMutableList().apply {
                    this[35] = 2.0 // Maxillary spacing
                    this[42] = 2.0 // Mandibular spacing
                }
            }
            7 -> {
                // Crossbite
                List(56) { 0.0 }.toMutableList().apply {
                    this[50] = 1.0 // Crossbite present
                }
            }
            8 -> {
                // Retrognathism
                List(56) { 0.0 }.toMutableList().apply {
                    this[12] = -2.0 // AP Skeletal Class II
                }
            }
            9 -> {
                // Prognathism
                List(56) { 0.0 }.toMutableList().apply {
                    this[12] = 2.0 // AP Skeletal Class III
                }
            }
            10 -> {
                // Mouth breathing
                List(56) { 0.0 }.toMutableList().apply {
                    this[23] = 2.0 // Predominantly oral breathing
                }
            }
            11 -> {
                // Abnormal swallowing
                List(56) { 0.0 }.toMutableList().apply {
                    this[24] = 2.0 // Infantile pattern
                }
            }
            12 -> {
                // TMJ dysfunction
                List(56) { 0.0 }.toMutableList().apply {
                    this[29] = 2.0 // Clicking with pain
                }
            }
            13 -> {
                // Clicking jaw
                List(56) { 0.0 }.toMutableList().apply {
                    this[29] = 1.0 // Clicking only
                }
            }
            14 -> {
                // Open bite
                List(56) { 0.0 }.toMutableList().apply {
                    this[49] = 4.0 // Severe open bite
                }
            }
            15 -> {
                // Deep bite
                List(56) { 0.0 }.toMutableList().apply {
                    this[49] = -5.0 // True deep bite
                }
            }
            else -> List(56) { 0.0 } // Default: all zeros
        }
    }
    
    /**
     * Calculate normalization constant for Bayesian inference
     */
    private fun calculateNormalizationConstant(featureVector: List<Double>): Double {
        var sum = 0.0
        for (vectorId in 0..15) {
            val prior = MALOCCLUSION_PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val diseaseVector = getDiseaseVector(vectorId)
            val likelihood = calculateCosineSimilarity(featureVector, diseaseVector)
            sum += likelihood * prior
        }
        return if (sum > 0.0) sum else 1.0
    }
    
    private fun createDiseaseVector(id: Int, name: String, icd11Primary: String, icd11Secondary: String): MalocclusionDiseaseVector {
        val vectorInfo = MALOCCLUSION_DISEASE_VECTORS[id] ?: MalocclusionDiseaseInfo(name, icd11Primary, icd11Secondary)
        return MalocclusionDiseaseVector(
            id = id,
            name = vectorInfo.name,
            icd11Primary = vectorInfo.icd11Primary,
            icd11Secondary = vectorInfo.icd11Secondary,
            probability = 1.0, // Symbolic match = 100% confidence
            cosineSimilarity = 1.0,
            symbolicMatch = true
        )
    }
    
    // Parsing functions for all 56 parameters
    private fun parseShapeOfHead(value: String): Double {
        return when {
            value.contains("+1") || value.contains("Brachycephalic") -> 1.0
            value.contains("0") || value.contains("Mesocephalic") -> 0.0
            value.contains("-1") || value.contains("Dolichocephalic") -> -1.0
            else -> 0.0
        }
    }
    
    private fun parseFacialSymmetry(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("Symmetric") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseFacialForm(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Extremely convex") -> 3.0
            value.contains("+2") || value.contains("Convex") && !value.contains("Mildly") -> 2.0
            value.contains("+1") || value.contains("Mildly convex") -> 1.0
            value.contains("0") || value.contains("Straight") -> 0.0
            value.contains("-1") || value.contains("Mildly concave") -> -1.0
            value.contains("-2") || value.contains("Concave") && !value.contains("Mildly") -> -2.0
            value.contains("-3") || value.contains("Extremely concave") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseFacialDivergence(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Anterior") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-2") || value.contains("Posterior") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseInterlabialGap(value: String): Double {
        return when {
            value.contains("+4") || value.contains("Extreme") -> 4.0
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("Competent") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseNasolabialAngle(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Extremely obtuse") -> 3.0
            value.contains("+2") || value.contains("Obtuse") && !value.contains("Mildly") -> 2.0
            value.contains("+1") || value.contains("Mildly obtuse") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Mildly acute") -> -1.0
            value.contains("-2") || value.contains("Acute") && !value.contains("Mildly") -> -2.0
            value.contains("-3") || value.contains("Extremely acute") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseUpperLipPosture(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Everted") -> 2.0
            value.contains("+1") || value.contains("Normal") -> 1.0
            value.contains("0") || value.contains("Normal length") -> 0.0
            value.contains("-1") || value.contains("Short") -> -1.0
            else -> 0.0
        }
    }
    
    private fun parseLowerLipPosture(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Everted") -> 2.0
            value.contains("+1") || value.contains("Normal") -> 1.0
            value.contains("0") || value.contains("Normal length") -> 0.0
            value.contains("-1") || value.contains("Short") -> -1.0
            value.contains("-2") || value.contains("Lip trap") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseLipTonicity(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Hyperactive") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-2") || value.contains("Hypoactive") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseRestingLowerLipLine(value: String): Double {
        return when {
            value.contains("+2") || value.contains("High") -> 2.0
            value.contains("+1") || value.contains("Above average") -> 1.0
            value.contains("0") || value.contains("Average") -> 0.0
            value.contains("-1") || value.contains("Low") && !value.contains("Very") -> -1.0
            value.contains("-2") || value.contains("Very low") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseMentolabialSulcus(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Very shallow") -> 2.0
            value.contains("+1") || value.contains("Shallow") && !value.contains("Very") -> 1.0
            value.contains("0") || value.contains("Average") -> 0.0
            value.contains("-1") || value.contains("Deep") && !value.contains("Very") -> -1.0
            value.contains("-2") || value.contains("Very deep") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseChinPosition(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severely protruding") -> 3.0
            value.contains("+2") || value.contains("Moderately protruding") -> 2.0
            value.contains("+1") || value.contains("Mildly protruding") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Mildly receding") -> -1.0
            value.contains("-2") || value.contains("Moderately receding") -> -2.0
            value.contains("-3") || value.contains("Severely receding") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseApSkeletalRelationship(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") && value.contains("Class III") -> 3.0
            value.contains("+2") || value.contains("Moderate") && value.contains("Class III") -> 2.0
            value.contains("+1") || value.contains("Mild") && value.contains("Class III") -> 1.0
            value.contains("0") || value.contains("Class I") -> 0.0
            value.contains("-1") || value.contains("Mild") && value.contains("Class II") -> -1.0
            value.contains("-2") || value.contains("Moderate") && value.contains("Class II") -> -2.0
            value.contains("-3") || value.contains("Severe") && value.contains("Class II") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseTotalFaceHeight(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severely increased") -> 3.0
            value.contains("+2") || value.contains("Moderately increased") -> 2.0
            value.contains("+1") || value.contains("Mildly increased") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Mildly decreased") -> -1.0
            value.contains("-2") || value.contains("Moderately decreased") -> -2.0
            value.contains("-3") || value.contains("Severely decreased") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseUpperFaceHeight(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Increased") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-2") || value.contains("Decreased") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseMiddleFaceHeight(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Increased") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-2") || value.contains("Decreased") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseLowerFaceHeight(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severely increased") -> 3.0
            value.contains("+2") || value.contains("Moderately increased") -> 2.0
            value.contains("+1") || value.contains("Mildly increased") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Mildly decreased") -> -1.0
            value.contains("-2") || value.contains("Moderately decreased") -> -2.0
            value.contains("-3") || value.contains("Severely decreased") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseVto(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Positive") -> 2.0
            value.contains("0") || value.contains("Neutral") -> 0.0
            value.contains("-2") || value.contains("Negative") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseClinicalFmpa(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe hyperdivergent") -> 3.0
            value.contains("+2") || value.contains("Moderate hyperdivergent") -> 2.0
            value.contains("+1") || value.contains("Mild hyperdivergent") -> 1.0
            value.contains("0") || value.contains("Average") -> 0.0
            value.contains("-1") || value.contains("Mild hypodivergent") -> -1.0
            value.contains("-2") || value.contains("Moderate hypodivergent") -> -2.0
            value.contains("-3") || value.contains("Severe hypodivergent") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseIncisorExposureAtRest(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Excessive") -> 3.0
            value.contains("+2") || value.contains("Increased") && !value.contains("Slightly") -> 2.0
            value.contains("+1") || value.contains("Slightly increased") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Decreased") -> -1.0
            value.contains("-2") || value.contains("None") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseIncisorExposureAtSpeech(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Increased") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-2") || value.contains("Decreased") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseIncisorExposureOnSmiling(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Excessive gingival show") -> 3.0
            value.contains("+2") || value.contains("Increased gingival show") -> 2.0
            value.contains("+1") || value.contains("Mild gingival show") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Reduced tooth show") -> -1.0
            value.contains("-2") || value.contains("Minimal tooth show") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseRespiratoryPattern(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Obligate oral") -> 3.0
            value.contains("+2") || value.contains("Predominantly oral") -> 2.0
            value.contains("+1") || value.contains("Oro-nasal mixed") -> 1.0
            value.contains("0") || value.contains("Nasal breathing") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseDeglutition(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe tongue thrust") -> 3.0
            value.contains("+2") || value.contains("Infantile pattern") -> 2.0
            value.contains("+1") || value.contains("Transitional") -> 1.0
            value.contains("0") || value.contains("Normal mature") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMastication(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Abnormal with compensations") -> 3.0
            value.contains("+2") || value.contains("Unilateral only") -> 2.0
            value.contains("+1") || value.contains("Predominantly unilateral") -> 1.0
            value.contains("0") || value.contains("Normal bilateral") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseSpeech(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe impediment") -> 3.0
            value.contains("+2") || value.contains("Moderate issues") -> 2.0
            value.contains("+1") || value.contains("Mild issues") -> 1.0
            value.contains("0") || value.contains("Normal articulation") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parsePathOfClosure(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Complex deviation") -> 3.0
            value.contains("+2") || value.contains("Backward shift") -> 2.0
            value.contains("+1") || value.contains("Forward shift") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLateralPathOfClosure(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Deflection with shift") -> 3.0
            value.contains("+2") || value.contains("Moderate deviation") -> 2.0
            value.contains("+1") || value.contains("Mild deviation") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseTmjFunction(value: String): Double {
        return when {
            value.contains("+5") || value.contains("Severe dysfunction") -> 5.0
            value.contains("+4") || value.contains("Locking") -> 4.0
            value.contains("+3") || value.contains("Pain with limitation") -> 3.0
            value.contains("+2") || value.contains("Clicking with pain") -> 2.0
            value.contains("+1") || value.contains("Clicking only") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parsePosturalRestPositionOfTongue(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severely low/forward") -> 3.0
            value.contains("+2") || value.contains("Low posture") -> 2.0
            value.contains("+1") || value.contains("Slightly low") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryArchShape(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Square") -> 2.0
            value.contains("0") || value.contains("Average") || value.contains("U-shaped") -> 0.0
            value.contains("-2") || value.contains("Tapered") -> -2.0
            value.contains("-3") || value.contains("Severely constricted") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryArchSymmetry(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe asymmetry") -> 3.0
            value.contains("+2") || value.contains("Moderate asymmetry") -> 2.0
            value.contains("+1") || value.contains("Mild asymmetry") -> 1.0
            value.contains("0") || value.contains("Symmetric") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryCrowding(value: String): Double {
        return when {
            value.contains("-3") || value.contains("Severe") -> -3.0
            value.contains("-2") || value.contains("Moderate") -> -2.0
            value.contains("-1") || value.contains("Mild") -> -1.0
            value.contains("0") || value.contains("No crowding") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillarySpacing(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("No spacing") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryRotation(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("No rotation") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryAxialInclination(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe tipping") -> 3.0
            value.contains("+2") || value.contains("Moderate tipping") -> 2.0
            value.contains("+1") || value.contains("Mild tipping") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaxillaryTransposition(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Complete transposition") -> 2.0
            value.contains("+1") || value.contains("Partial transposition") -> 1.0
            value.contains("0") || value.contains("No transposition") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularArchShape(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Square") -> 2.0
            value.contains("0") || value.contains("Average") || value.contains("U-shaped") -> 0.0
            value.contains("-2") || value.contains("Tapered") -> -2.0
            value.contains("-3") || value.contains("Severely constricted") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularArchSymmetry(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe asymmetry") -> 3.0
            value.contains("+2") || value.contains("Moderate asymmetry") -> 2.0
            value.contains("+1") || value.contains("Mild asymmetry") -> 1.0
            value.contains("0") || value.contains("Symmetric") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularCrowding(value: String): Double {
        return when {
            value.contains("-3") || value.contains("Severe") -> -3.0
            value.contains("-2") || value.contains("Moderate") -> -2.0
            value.contains("-1") || value.contains("Mild") -> -1.0
            value.contains("0") || value.contains("No crowding") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularSpacing(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("No spacing") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularRotation(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe") -> 3.0
            value.contains("+2") || value.contains("Moderate") -> 2.0
            value.contains("+1") || value.contains("Mild") -> 1.0
            value.contains("0") || value.contains("No rotation") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularAxialInclination(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe tipping") -> 3.0
            value.contains("+2") || value.contains("Moderate tipping") -> 2.0
            value.contains("+1") || value.contains("Mild tipping") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMandibularTransposition(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Complete transposition") -> 2.0
            value.contains("+1") || value.contains("Partial transposition") -> 1.0
            value.contains("0") || value.contains("No transposition") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMaximumMouthOpening(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Hypermobile") -> 2.0
            value.contains("+1") || value.contains("Increased") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Reduced") -> -1.0
            value.contains("-2") || value.contains("Severely limited") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseFreewaySpace(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Excessive") -> 2.0
            value.contains("+1") || value.contains("Increased") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Reduced") -> -1.0
            value.contains("-2") || value.contains("Minimal") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseCurveOfSpee(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severely exaggerated") -> 3.0
            value.contains("+2") || value.contains("Moderately exaggerated") -> 2.0
            value.contains("+1") || value.contains("Mildly exaggerated") -> 1.0
            value.contains("0") || value.contains("Normal") || value.contains("Flat") -> 0.0
            value.contains("-1") || value.contains("Reverse curve") -> -1.0
            else -> 0.0
        }
    }
    
    private fun parseMolarRelationship(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Full Class III") -> 3.0
            value.contains("+2") || value.contains("End-to-end Class III") -> 2.0
            value.contains("+1") || value.contains("Mild Class III tendency") -> 1.0
            value.contains("0") || value.contains("Class I") -> 0.0
            value.contains("-1") || value.contains("Mild Class II tendency") -> -1.0
            value.contains("-2") || value.contains("End-to-end Class II") -> -2.0
            value.contains("-3") || value.contains("Full Class II Division 1") -> -3.0
            value.contains("-4") || value.contains("Full Class II Division 2") -> -4.0
            else -> 0.0
        }
    }
    
    private fun parseCanineRelationship(value: String): Double {
        return when {
            value.contains("+2") || value.contains("Class III canine") -> 2.0
            value.contains("+1") || value.contains("End-to-end Class III") -> 1.0
            value.contains("0") || value.contains("Class I canine") -> 0.0
            value.contains("-1") || value.contains("End-to-end Class II") -> -1.0
            value.contains("-2") || value.contains("Class II canine") -> -2.0
            else -> 0.0
        }
    }
    
    private fun parseIncisorRelationship(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe anterior crossbite") -> 3.0
            value.contains("+2") || value.contains("Moderate anterior crossbite") -> 2.0
            value.contains("+1") || value.contains("Edge-to-edge") -> 1.0
            value.contains("0") || value.contains("Class I normal") -> 0.0
            value.contains("-1") || value.contains("Class II Division 2 pattern") -> -1.0
            value.contains("-2") || value.contains("Class II Division 1 mild") -> -2.0
            value.contains("-3") || value.contains("Class II Division 1 moderate") -> -3.0
            value.contains("-4") || value.contains("Class II Division 1 severe") -> -4.0
            else -> 0.0
        }
    }
    
    private fun parseOverjet(value: String): Double {
        return when {
            value.contains("+4") || value.contains("Reverse overjet severe") -> 4.0
            value.contains("+3") || value.contains("Reverse overjet moderate") -> 3.0
            value.contains("+2") || value.contains("Reverse overjet mild") -> 2.0
            value.contains("+1") || value.contains("Edge-to-edge") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Increased") && !value.contains("Severe") && !value.contains("Extreme") -> -1.0
            value.contains("-2") || value.contains("Severe increased") -> -2.0
            value.contains("-3") || value.contains("Extreme increased") -> -3.0
            value.contains("-4") || value.contains("Very extreme") -> -4.0
            else -> 0.0
        }
    }
    
    private fun parseOverbite(value: String): Double {
        return when {
            value.contains("+4") || value.contains("Severe open bite") -> 4.0
            value.contains("+3") || value.contains("Moderate open bite") -> 3.0
            value.contains("+2") || value.contains("Mild open bite") -> 2.0
            value.contains("+1") || value.contains("Reduced overbite") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            value.contains("-1") || value.contains("Increased") && !value.contains("Deep") -> -1.0
            value.contains("-2") || value.contains("Deep bite") && !value.contains("Severe") && !value.contains("Complete") && !value.contains("True") && !value.contains("Pseudo") -> -2.0
            value.contains("-3") || value.contains("Severe deep bite") -> -3.0
            value.contains("-4") || value.contains("Complete deep bite") -> -4.0
            value.contains("-5") || value.contains("True deep bite") -> -5.0
            value.contains("-6") || value.contains("Pseudo deep bite") -> -6.0
            else -> 0.0
        }
    }
    
    private fun parseCrossbiteType(value: String): Double {
        return when {
            value.contains("+7") || value.contains("Complete crossbite") -> 7.0
            value.contains("+6") || value.contains("Scissors bite") -> 6.0
            value.contains("+5") || value.contains("Bilateral posterior crossbite") -> 5.0
            value.contains("+4") || value.contains("Unilateral posterior crossbite") -> 4.0
            value.contains("+3") || value.contains("Anterior crossbite") && value.contains(">4") -> 3.0
            value.contains("+2") || value.contains("Anterior crossbite") && value.contains("3-4") -> 2.0
            value.contains("+1") || value.contains("Anterior crossbite") && value.contains("1-2") -> 1.0
            value.contains("0") || value.contains("No crossbite") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseUpperDentalMidline(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Deviated right") && value.contains(">3mm") -> 3.0
            value.contains("+2") || value.contains("Deviated right") && value.contains("2-3mm") -> 2.0
            value.contains("+1") || value.contains("Deviated right") && value.contains("1-2mm") -> 1.0
            value.contains("0") || value.contains("Coincident") -> 0.0
            value.contains("-1") || value.contains("Deviated left") && value.contains("1-2mm") -> -1.0
            value.contains("-2") || value.contains("Deviated left") && value.contains("2-3mm") -> -2.0
            value.contains("-3") || value.contains("Deviated left") && value.contains(">3mm") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseLowerDentalMidline(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Deviated right") && value.contains(">3mm") -> 3.0
            value.contains("+2") || value.contains("Deviated right") && value.contains("2-3mm") -> 2.0
            value.contains("+1") || value.contains("Deviated right") && value.contains("1-2mm") -> 1.0
            value.contains("0") || value.contains("Coincident") -> 0.0
            value.contains("-1") || value.contains("Deviated left") && value.contains("1-2mm") -> -1.0
            value.contains("-2") || value.contains("Deviated left") && value.contains("2-3mm") -> -2.0
            value.contains("-3") || value.contains("Deviated left") && value.contains(">3mm") -> -3.0
            else -> 0.0
        }
    }
    
    private fun parseInterArchMidlineDiscrepancy(value: String): Double {
        return when {
            value.contains("+3") || value.contains("Severe discrepancy") -> 3.0
            value.contains("+2") || value.contains("Moderate discrepancy") -> 2.0
            value.contains("+1") || value.contains("Mild discrepancy") -> 1.0
            value.contains("0") || value.contains("Coincident midlines") -> 0.0
            else -> 0.0
        }
    }
    
    companion object {
        // Prior probabilities (textbook-based)
        val MALOCCLUSION_PRIOR_PROBABILITIES = mapOf(
            0 to 0.200,  // Normal Occlusion
            1 to 0.300,  // Malocclusion, Angle Class I
            2 to 0.200,  // Malocclusion, Angle Class II Division 1
            3 to 0.080,  // Malocclusion, Angle Class II Division 2
            4 to 0.100,  // Malocclusion, Angle Class III
            5 to 0.050,  // Crowding of Teeth
            6 to 0.030,  // Excessive Spacing of Teeth
            7 to 0.020,  // Reverse Articulation (Crossbite)
            8 to 0.010,  // Retrognathism
            9 to 0.010,  // Prognathism
            10 to 0.005, // Malocclusion due to Mouth Breathing
            11 to 0.005, // Malocclusion due to Abnormal Swallowing
            12 to 0.005, // TMJ-Pain-Dysfunction Syndrome
            13 to 0.005, // Clicking Jaw
            14 to 0.010, // Severe Open Bite
            15 to 0.010  // Severe Deep Bite
        )
        
        val MALOCCLUSION_DISEASE_VECTORS = mapOf(
            0 to MalocclusionDiseaseInfo("Normal Occlusion", "None", ""),
            1 to MalocclusionDiseaseInfo("Malocclusion, Angle Class I", "DA0C.20", ""),
            2 to MalocclusionDiseaseInfo("Malocclusion, Angle Class II Division 1", "DA0C.21", ""),
            3 to MalocclusionDiseaseInfo("Malocclusion, Angle Class II Division 2", "DA0C.21", ""),
            4 to MalocclusionDiseaseInfo("Malocclusion, Angle Class III", "DA0C.22", ""),
            5 to MalocclusionDiseaseInfo("Crowding of Teeth", "DA0C.24", ""),
            6 to MalocclusionDiseaseInfo("Excessive Spacing of Teeth", "DA0C.23", ""),
            7 to MalocclusionDiseaseInfo("Reverse Articulation (Crossbite)", "DA0C.26", ""),
            8 to MalocclusionDiseaseInfo("Retrognathism (Mandibular)", "DA0C.12", ""),
            9 to MalocclusionDiseaseInfo("Prognathism (Mandibular)", "DA0C.11", ""),
            10 to MalocclusionDiseaseInfo("Malocclusion due to Mouth Breathing", "DA0C.52", ""),
            11 to MalocclusionDiseaseInfo("Malocclusion due to Abnormal Swallowing", "DA0C.51", ""),
            12 to MalocclusionDiseaseInfo("Temporomandibular Joint-Pain-Dysfunction Syndrome", "DA0C.60", ""),
            13 to MalocclusionDiseaseInfo("Clicking Jaw", "DA0C.61", ""),
            14 to MalocclusionDiseaseInfo("Severe Open Bite", "DA0C.2Y", ""),
            15 to MalocclusionDiseaseInfo("Severe Deep Bite", "DA0C.2Y", "")
        )
        
        data class MalocclusionDiseaseInfo(
            val name: String,
            val icd11Primary: String,
            val icd11Secondary: String
        )
    }
}

















