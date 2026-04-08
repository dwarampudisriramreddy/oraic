package com.ram.orai.oraic

/**
 * Complete Oral Mucosa Fuzzy Diagnostic System
 * Based on Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
 */

data class OralMucosaDiagnosis(
    val primaryDiagnosis: OralMucosaDiseaseVector?,
    val differentialDiagnoses: List<OralMucosaDiseaseVector>,
    val icd11Codes: List<String>
)

data class OralMucosaDiseaseVector(
    val id: Int,
    val name: String,
    val icd11Primary: String,
    val icd11Secondary: String = "",
    val probability: Double,
    val cosineSimilarity: Double = 0.0,
    val symbolicMatch: Boolean = false
)

class OralMucosaDiagnosticEngine {
    
    /**
     * Calculate diagnosis using Vector Cosine Similarity, Symbolic Logic, and Bayesian Inference
     */
    fun diagnose(patientInfo: PatientInfo): OralMucosaDiagnosis {
        // Extract feature vector from patientInfo (20 lesion characteristics)
        val featureVector = extractFeatureVector(patientInfo)
        
        // Step 1: Apply Symbolic Logic Rules (highest priority)
        val symbolicDiagnosis = applySymbolicLogic(featureVector)
        if (symbolicDiagnosis != null) {
            return OralMucosaDiagnosis(
                primaryDiagnosis = symbolicDiagnosis,
                differentialDiagnoses = emptyList(),
                icd11Codes = listOf(symbolicDiagnosis.icd11Primary).filter { it.isNotEmpty() }
            )
        }
        
        // Step 2: Calculate Vector Cosine Similarity for all disease vectors
        val cosineSimilarities = (1..ORAL_MUCOSA_DISEASE_VECTORS.size).map { vectorId ->
            val diseaseVector = getDiseaseVector(vectorId)
            val similarity = calculateCosineSimilarity(featureVector, diseaseVector)
            Pair(vectorId, similarity)
        }.sortedByDescending { it.second }
        
        // Step 3: Apply Bayesian Inference to top matches
        val topCandidates = cosineSimilarities.take(10) // Top 10 by cosine similarity
        val bayesianProbabilities = topCandidates.map { (vectorId, cosineSim) ->
            val prior = ORAL_MUCOSA_PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val likelihood = cosineSim // Use cosine similarity as likelihood
            val posterior = (likelihood * prior) / calculateNormalizationConstant(featureVector)
            
            val vectorInfo = ORAL_MUCOSA_DISEASE_VECTORS[vectorId] ?: OralMucosaDiseaseInfo("Unknown", "", "")
            OralMucosaDiseaseVector(
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
        
        return OralMucosaDiagnosis(
            primaryDiagnosis = primary,
            differentialDiagnoses = differentials,
            icd11Codes = listOfNotNull(primary?.icd11Primary).filter { it.isNotEmpty() }
        )
    }
    
    /**
     * Extract feature vector from PatientInfo (20 lesion characteristics)
     */
    private fun extractFeatureVector(patientInfo: PatientInfo): List<Double> {
        return listOf(
            parseLesionType(patientInfo.lesionType),
            parseLesionSize(patientInfo.lesionSize),
            parseLesionColor(patientInfo.lesionColor),
            parseSurfaceTexture(patientInfo.surfaceTexture),
            parseBorderDefinition(patientInfo.borderDefinition),
            parseInduration(patientInfo.induration),
            parseLesionPain(patientInfo.lesionPain),
            parseBurningSensation(patientInfo.burningSensation),
            parseBleedingTendency(patientInfo.bleedingTendency),
            parseLesionDuration(patientInfo.lesionDuration),
            parsePatternDistribution(patientInfo.patternDistribution),
            parseMobilityFixation(patientInfo.mobilityFixation),
            parseLymphadenopathy(patientInfo.lymphadenopathy),
            parseAssociatedSystemicSymptoms(patientInfo.associatedSystemicSymptoms),
            parseRemovability(patientInfo.removability),
            parseGrowthRate(patientInfo.growthRate),
            parseNikolskySign(patientInfo.nikolskySign),
            parseWickhamsStriae(patientInfo.wickhamsStriae),
            parseRecurrencePattern(patientInfo.recurrencePattern),
            parseFunctionalImpairment(patientInfo.functionalImpairment)
        )
    }
    
    /**
     * Apply Symbolic Logic Rules (highest priority - textbook rules)
     */
    private fun applySymbolicLogic(features: List<Double>): OralMucosaDiseaseVector? {
        val lesionType = features[0]
        val color = features[2]
        val texture = features[3]
        val border = features[4]
        val induration = features[5]
        val pain = features[6]
        val burningSensation = features[7]
        val bleeding = features[8]
        val duration = features[9]
        val pattern = features[10]
        val mobility = features[11]
        val lymph = features[12]
        val systemic = features[13]
        val removability = features[14]
        val growth = features[15]
        val nikolsky = features[16]
        val wickhams = features[17]
        val recurrence = features[18]
        
        // Rule 1: Normal mucosa (all features normal)
        if (lesionType == 0.0 && color == 0.0 && texture == 0.0 && 
            border == 0.0 && induration == 0.0 && pain == 0.0 &&
            bleeding == 0.0 && pattern == 0.0 && mobility == 0.0 &&
            lymph == 0.0 && systemic == 0.0 && growth == 0.0 &&
            nikolsky == 0.0 && wickhams == 0.0 && recurrence == 0.0) {
            return createDiseaseVector(0, "Normal Oral Mucosa", "None", "")
        }
        
        // Rule 2: Pemphigus Vulgaris (Nikolsky positive, vesicles/bullae, systemic symptoms)
        if (nikolsky >= 6.0 && (lesionType == 5.0 || lesionType == 6.0) && systemic >= 3.0) {
            return createDiseaseVector(1, "Pemphigus Vulgaris", "DA00.30", "")
        }
        
        // Rule 3: Mucous Membrane Pemphigoid (Nikolsky positive, bullae, no systemic)
        if (nikolsky >= 4.0 && lesionType == 6.0 && systemic < 2.0) {
            return createDiseaseVector(2, "Mucous Membrane Pemphigoid", "DA00.31", "")
        }
        
        // Rule 4: Oral Lichen Planus (Wickham's striae, reticular pattern, white color)
        if (wickhams >= 4.0 && pattern == 8.0 && (color <= -7.0 || color == -9.0)) {
            return createDiseaseVector(3, "Oral Lichen Planus", "DA00.70", "")
        }
        
        // Rule 5: Leukoplakia (White lesion, adherent, no striae)
        if (color <= -7.0 && color >= -9.0 && removability <= -6.0 && wickhams == 0.0) {
            return createDiseaseVector(4, "Leukoplakia (Homogeneous)", "DA00.10", "")
        }
        
        // Rule 6: Non-homogeneous Leukoplakia (White, verrucous texture)
        if (color <= -7.0 && texture >= 8.0 && removability <= -6.0) {
            return createDiseaseVector(5, "Leukoplakia (Non-homogeneous)", "DA00.11", "")
        }
        
        // Rule 7: Erythroplakia (Red lesion, well-defined, indurated)
        if (color >= 4.0 && border >= 4.0 && induration >= 6.0) {
            return createDiseaseVector(6, "Erythroplakia", "DA00.80", "")
        }
        
        // Rule 8: Recurrent Aphthous Stomatitis (Recurrent ulcers, painful, no induration)
        if (lesionType <= -7.0 && pain >= 2.0 && recurrence >= 2.0 && induration <= 2.0) {
            when {
                pattern == 2.0 -> return createDiseaseVector(7, "Recurrent Aphthous Stomatitis (Herpetiform)", "DA00.22", "")
                lesionType <= -9.0 -> return createDiseaseVector(8, "Recurrent Aphthous Stomatitis (Major)", "DA00.21", "")
                else -> return createDiseaseVector(9, "Recurrent Aphthous Stomatitis (Minor)", "DA00.20", "")
            }
        }
        
        // Rule 9: Oral Candidiasis (Removable white coating, burning)
        if (removability >= 6.0 && (color <= -7.0 || color == -4.0) && burningSensation >= 2.0) {
            return createDiseaseVector(10, "Oral Candidiasis", "DA00.60", "")
        }
        
        // Rule 10: Acute Pseudomembranous Candidiasis (Easily removable white)
        if (removability >= 8.0 && color <= -7.0) {
            return createDiseaseVector(11, "Acute Pseudomembranous Candidiasis", "DA00.61", "")
        }
        
        // Rule 11: Oral Squamous Cell Carcinoma (Indurated, fixed, rapid growth, lymphadenopathy)
        if (induration >= 8.0 && mobility >= 6.0 && growth >= 8.0 && lymph >= 4.0) {
            return createDiseaseVector(12, "Oral Squamous Cell Carcinoma", "DA00.90", "")
        }
        
        // Rule 12: Traumatic Ulcer (Acute, painful, no recurrence)
        if (lesionType <= -7.0 && duration <= -8.0 && pain >= 2.0 && recurrence <= -8.0) {
            return createDiseaseVector(13, "Traumatic Ulcer", "DA00.23", "")
        }
        
        // Rule 13: Squamous Papilloma (Exophytic, verrucous, mobile)
        if (lesionType == 10.0 && texture >= 8.0 && mobility <= -6.0) {
            return createDiseaseVector(14, "Squamous Papilloma", "DA00.40", "")
        }
        
        // Rule 14: Amalgam Tattoo (Gray-black, stable, no symptoms)
        if (color == 9.0 && growth == 0.0 && pain == 0.0 && bleeding == 0.0) {
            return createDiseaseVector(15, "Amalgam Tattoo", "DA00.52", "")
        }
        
        // Rule 15: Geographic Tongue (Erythema migrans pattern)
        if (color >= 2.0 && pattern == 8.0 && recurrence >= 4.0 && pain <= 1.0) {
            return createDiseaseVector(16, "Erythema Migrans (Geographic Tongue)", "DA00.00", "")
        }
        
        // Rule 16: Oral Herpes Simplex (Vesicles, grouped, acute, painful)
        if (lesionType == 5.0 && pattern == 2.0 && duration <= -6.0 && pain >= 3.0) {
            return createDiseaseVector(17, "Oral Herpes Simplex Infection", "DA00.63", "")
        }
        
        // Rule 17: Oral Submucous Fibrosis (White, bilateral, indurated, functional impairment)
        if (color <= -7.0 && pattern == -4.0 && induration >= 6.0 && features[19] >= 4.0) {
            return createDiseaseVector(18, "Oral Submucous Fibrosis", "DA00.82", "")
        }
        
        // Rule 18: Erythema Multiforme (Target lesions, vesicles, systemic)
        if (pattern == 10.0 && (lesionType == 5.0 || lesionType == 6.0) && systemic >= 2.0) {
            return createDiseaseVector(19, "Erythema Multiforme Major", "DA00.33", "")
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
        // Return expected feature vector for each disease type (20 features)
        return when (vectorId) {
            0 -> List(20) { 0.0 } // Normal
            1 -> listOf(6.0, 4.0, 4.0, 0.0, 4.0, 0.0, 6.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0, 8.0, 0.0, 0.0, 6.0) // Pemphigus Vulgaris
            2 -> listOf(6.0, 4.0, 3.0, 0.0, 4.0, 0.0, 3.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 0.0, 0.0, 4.0) // Mucous Membrane Pemphigoid
            3 -> listOf(0.0, 2.0, -8.0, 4.0, 6.0, 0.0, 2.0, 2.0, 0.0, 6.0, 8.0, 0.0, 0.0, 0.0, -8.0, 0.0, 0.0, 8.0, 6.0, 2.0) // Oral Lichen Planus
            4 -> listOf(0.0, 4.0, -9.0, 0.0, 6.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, -8.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Leukoplakia Homogeneous
            5 -> listOf(9.0, 4.0, -8.0, 8.0, 6.0, 2.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, -6.0, 2.0, 0.0, 0.0, 0.0, 0.0) // Leukoplakia Non-homogeneous
            6 -> listOf(4.0, 4.0, 5.0, 0.0, 8.0, 8.0, 2.0, 0.0, 2.0, 8.0, 0.0, 6.0, 4.0, 0.0, -8.0, 8.0, 0.0, 0.0, 0.0, 4.0) // Erythroplakia
            7 -> listOf(-8.0, -2.0, 2.0, 0.0, 4.0, 0.0, 4.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 2.0) // RAS Herpetiform
            8 -> listOf(-9.0, 4.0, 2.0, 0.0, 4.0, 0.0, 6.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 4.0) // RAS Major
            9 -> listOf(-7.0, -2.0, 2.0, 0.0, 4.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 1.0) // RAS Minor
            10 -> listOf(-4.0, 0.0, -7.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, -2.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Oral Candidiasis
            11 -> listOf(-4.0, 0.0, -9.0, 0.0, 0.0, 0.0, 0.0, 3.0, 0.0, -8.0, 0.0, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Pseudomembranous Candidiasis
            12 -> listOf(10.0, 8.0, 4.0, 10.0, 10.0, 10.0, 4.0, 0.0, 4.0, 8.0, 0.0, 10.0, 8.0, 0.0, -10.0, 10.0, 0.0, 0.0, 0.0, 8.0) // SCC
            13 -> listOf(-8.0, -2.0, 2.0, 0.0, 4.0, 0.0, 4.0, 0.0, 0.0, -8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -10.0, 0.0) // Traumatic Ulcer
            14 -> listOf(10.0, 2.0, 0.0, 8.0, 6.0, 0.0, 0.0, 0.0, 0.0, 4.0, 0.0, -8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Squamous Papilloma
            15 -> listOf(0.0, -2.0, 9.0, 0.0, 4.0, 0.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // Amalgam Tattoo
            16 -> listOf(0.0, 0.0, 3.0, 0.0, 4.0, 0.0, 1.0, 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 0.0) // Geographic Tongue
            17 -> listOf(5.0, -2.0, 2.0, 0.0, 4.0, 0.0, 5.0, 0.0, 0.0, -8.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -8.0, 2.0) // Herpes Simplex
            18 -> listOf(0.0, 4.0, -8.0, 0.0, 4.0, 8.0, 2.0, 0.0, 0.0, 8.0, -4.0, 6.0, 0.0, 0.0, -8.0, 0.0, 0.0, 0.0, 0.0, 6.0) // OSMF
            19 -> listOf(6.0, 2.0, 4.0, 0.0, 6.0, 0.0, 4.0, 0.0, 0.0, -6.0, 10.0, 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0) // Erythema Multiforme
            else -> List(20) { 0.0 } // Default: all zeros
        }
    }
    
    /**
     * Calculate normalization constant for Bayesian inference
     */
    private fun calculateNormalizationConstant(featureVector: List<Double>): Double {
        var sum = 0.0
        for (vectorId in 0..19) {
            val prior = ORAL_MUCOSA_PRIOR_PROBABILITIES[vectorId] ?: 0.0
            val diseaseVector = getDiseaseVector(vectorId)
            val likelihood = calculateCosineSimilarity(featureVector, diseaseVector)
            sum += likelihood * prior
        }
        return if (sum > 0.0) sum else 1.0
    }
    
    private fun createDiseaseVector(id: Int, name: String, icd11Primary: String, icd11Secondary: String): OralMucosaDiseaseVector {
        val vectorInfo = ORAL_MUCOSA_DISEASE_VECTORS[id] ?: OralMucosaDiseaseInfo(name, icd11Primary, icd11Secondary)
        return OralMucosaDiseaseVector(
            id = id,
            name = vectorInfo.name,
            icd11Primary = vectorInfo.icd11Primary,
            icd11Secondary = vectorInfo.icd11Secondary,
            probability = 1.0, // Symbolic match = 100% confidence
            cosineSimilarity = 1.0,
            symbolicMatch = true
        )
    }
    
    // Parsing functions for all 20 parameters
    private fun parseLesionType(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-9") -> -9.0
            value.contains("-8") -> -8.0
            value.contains("-7") -> -7.0
            value.contains("-6") -> -6.0
            value.contains("-5") -> -5.0
            value.contains("-4") -> -4.0
            value.contains("-3") -> -3.0
            value.contains("-2") -> -2.0
            value.contains("-1") -> -1.0
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLesionSize(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("10mm") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLesionColor(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-9") -> -9.0
            value.contains("-8") -> -8.0
            value.contains("-7") -> -7.0
            value.contains("-6") -> -6.0
            value.contains("-5") -> -5.0
            value.contains("-4") -> -4.0
            value.contains("-3") -> -3.0
            value.contains("-2") -> -2.0
            value.contains("-1") -> -1.0
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseSurfaceTexture(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseBorderDefinition(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Moderately") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseInduration(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Normal") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLesionPain(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No pain") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseBurningSensation(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No burning") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseBleedingTendency(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No bleeding") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLesionDuration(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("1-2 months") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parsePatternDistribution(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Solitary") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseMobilityFixation(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Normal mobility") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseLymphadenopathy(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No palpable") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseAssociatedSystemicSymptoms(value: String): Double {
        return when {
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No systemic") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseRemovability(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Not applicable") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseGrowthRate(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Stable") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseNikolskySign(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Negative") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseWickhamsStriae(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("Absent") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseRecurrencePattern(value: String): Double {
        return when {
            value.contains("-10") -> -10.0
            value.contains("-8") -> -8.0
            value.contains("-6") -> -6.0
            value.contains("-4") -> -4.0
            value.contains("-2") -> -2.0
            value.contains("+10") -> 10.0
            value.contains("+8") -> 8.0
            value.contains("+6") -> 6.0
            value.contains("+4") -> 4.0
            value.contains("+2") -> 2.0
            value.contains("0") || value.contains("No recurrence") -> 0.0
            else -> 0.0
        }
    }
    
    private fun parseFunctionalImpairment(value: String): Double {
        return when {
            value.contains("+10") -> 10.0
            value.contains("+9") -> 9.0
            value.contains("+8") -> 8.0
            value.contains("+7") -> 7.0
            value.contains("+6") -> 6.0
            value.contains("+5") -> 5.0
            value.contains("+4") -> 4.0
            value.contains("+3") -> 3.0
            value.contains("+2") -> 2.0
            value.contains("+1") -> 1.0
            value.contains("0") || value.contains("No impairment") -> 0.0
            else -> 0.0
        }
    }
    
    companion object {
        // Prior probabilities (textbook-based)
        val ORAL_MUCOSA_PRIOR_PROBABILITIES = mapOf(
            0 to 0.400,  // Normal Oral Mucosa
            1 to 0.005,  // Pemphigus Vulgaris
            2 to 0.010,  // Mucous Membrane Pemphigoid
            3 to 0.080,  // Oral Lichen Planus
            4 to 0.150,  // Leukoplakia (Homogeneous)
            5 to 0.050,  // Leukoplakia (Non-homogeneous)
            6 to 0.010,  // Erythroplakia
            7 to 0.020,  // RAS Herpetiform
            8 to 0.030,  // RAS Major
            9 to 0.150,  // RAS Minor
            10 to 0.080, // Oral Candidiasis
            11 to 0.030, // Pseudomembranous Candidiasis
            12 to 0.005, // Oral Squamous Cell Carcinoma
            13 to 0.050, // Traumatic Ulcer
            14 to 0.020, // Squamous Papilloma
            15 to 0.010, // Amalgam Tattoo
            16 to 0.020, // Geographic Tongue
            17 to 0.010, // Oral Herpes Simplex
            18 to 0.005, // Oral Submucous Fibrosis
            19 to 0.010  // Erythema Multiforme
        )
        
        val ORAL_MUCOSA_DISEASE_VECTORS = mapOf(
            0 to OralMucosaDiseaseInfo("Normal Oral Mucosa", "None", ""),
            1 to OralMucosaDiseaseInfo("Pemphigus Vulgaris", "DA00.30", ""),
            2 to OralMucosaDiseaseInfo("Mucous Membrane Pemphigoid", "DA00.31", ""),
            3 to OralMucosaDiseaseInfo("Oral Lichen Planus", "DA00.70", ""),
            4 to OralMucosaDiseaseInfo("Leukoplakia (Homogeneous)", "DA00.10", ""),
            5 to OralMucosaDiseaseInfo("Leukoplakia (Non-homogeneous)", "DA00.11", ""),
            6 to OralMucosaDiseaseInfo("Erythroplakia", "DA00.80", ""),
            7 to OralMucosaDiseaseInfo("Recurrent Aphthous Stomatitis (Herpetiform)", "DA00.22", ""),
            8 to OralMucosaDiseaseInfo("Recurrent Aphthous Stomatitis (Major)", "DA00.21", ""),
            9 to OralMucosaDiseaseInfo("Recurrent Aphthous Stomatitis (Minor)", "DA00.20", ""),
            10 to OralMucosaDiseaseInfo("Oral Candidiasis", "DA00.60", ""),
            11 to OralMucosaDiseaseInfo("Acute Pseudomembranous Candidiasis", "DA00.61", ""),
            12 to OralMucosaDiseaseInfo("Oral Squamous Cell Carcinoma", "DA00.90", ""),
            13 to OralMucosaDiseaseInfo("Traumatic Ulcer", "DA00.23", ""),
            14 to OralMucosaDiseaseInfo("Squamous Papilloma", "DA00.40", ""),
            15 to OralMucosaDiseaseInfo("Amalgam Tattoo", "DA00.52", ""),
            16 to OralMucosaDiseaseInfo("Erythema Migrans (Geographic Tongue)", "DA00.00", ""),
            17 to OralMucosaDiseaseInfo("Oral Herpes Simplex Infection", "DA00.63", ""),
            18 to OralMucosaDiseaseInfo("Oral Submucous Fibrosis", "DA00.82", ""),
            19 to OralMucosaDiseaseInfo("Erythema Multiforme Major", "DA00.33", "")
        )
        
        data class OralMucosaDiseaseInfo(
            val name: String,
            val icd11Primary: String,
            val icd11Secondary: String
        )
    }
}

