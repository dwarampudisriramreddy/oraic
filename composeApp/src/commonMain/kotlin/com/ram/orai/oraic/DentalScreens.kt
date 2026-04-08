package com.ram.orai.oraic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    stateHolder: DentalStateHolder,
    patient: Patient,
    patientRepository: PatientRepository,
    onBackToDashboard: () -> Unit,
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {}
) {
    val uiState = stateHolder.uiState
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Auto-save patient's dental state when it changes
    LaunchedEffect(uiState) {
        val updatedPatient = patient.copy(
            dentalState = uiState,
            updatedAt = System.currentTimeMillis() // Update timestamp every time
        )
        patientRepository.updatePatient(updatedPatient)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patient.displayName) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Patient Info"
                        )
                    },
                    label = { Text("Info") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Odontogram"
                        )
                    },
                    label = { Text("Odontogram") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Summarize,
                            contentDescription = "Summary"
                        )
                    },
                    label = { Text("Summary") }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PatientInfoScreen(stateHolder, uiState)
                1 -> OdontogramScreen(stateHolder, uiState)
                2 -> SummaryScreen(uiState, stateHolder)
            }
        }
    }
}

@Composable
fun ExpandableSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
fun ExpandableSectionHeaderSmall(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PatientInfoScreen(stateHolder: DentalStateHolder, state: DentalState) {
    // Define all tooth numbers
    val permanentTeeth = (11..18).toList() + (21..28).toList() + (31..38).toList() + (41..48).toList()
    val primaryTeeth = (51..55).toList() + (61..65).toList() + (71..75).toList() + (81..85).toList()
    val allTeeth = permanentTeeth + primaryTeeth
    
    // Filter tooth numbers based on dentition
    val toothNumbers = remember(state.patientInfo.dentition) {
        when (state.patientInfo.dentition) {
            "Permanent" -> permanentTeeth
            "Primary" -> primaryTeeth
            "Mixed" -> allTeeth
            "Edentulous" -> emptyList()
            else -> permanentTeeth
        }
    }
    
    var selectedToothNumber by remember(state.patientInfo.dentition) { 
        mutableStateOf(if (toothNumbers.isNotEmpty()) toothNumbers.first() else 11)
    }
    
    // Reset selected tooth if current selection is not in filtered list
    if (selectedToothNumber !in toothNumbers && toothNumbers.isNotEmpty()) {
        selectedToothNumber = toothNumbers.first()
    }
    
    var selectedSegmentNumber by remember { mutableStateOf(1) }
    
    // Toggle states for expandable sections
    var toothSectionExpanded by remember { mutableStateOf(false) }
    var periodontalSectionExpanded by remember { mutableStateOf(false) }
    var oralMucosaSectionExpanded by remember { mutableStateOf(false) }
    var malocclusionSectionExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Demographic Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.patientInfo.dob,
            onValueChange = { stateHolder.updatePatientInfo(state.patientInfo.copy(dob = it)) },
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        DropdownSelector(
            label = "Dentition",
            options = listOf("Permanent", "Mixed", "Primary", "Edentulous"),
            selectedOption = state.patientInfo.dentition,
            onSelected = { stateHolder.updatePatientInfo(state.patientInfo.copy(dentition = it)) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chief Complaint", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Chief Complaint Text Input
        OutlinedTextField(
            value = state.patientInfo.chiefComplaint,
            onValueChange = { stateHolder.updatePatientInfo(state.patientInfo.copy(chiefComplaint = it)) },
            label = { Text("Chief Complaint") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Region Dropdown - depends on dentition
        val regionOptions = remember(state.patientInfo.dentition) {
            val toothOptions = when (state.patientInfo.dentition) {
                "Primary" -> (51..55).toList() + (61..65).toList() + (71..75).toList() + (81..85).toList()
                "Permanent", "Mixed" -> (11..18).toList() + (21..28).toList() + (31..38).toList() + (41..48).toList()
                "Edentulous" -> emptyList<Int>()
                else -> (11..18).toList() + (21..28).toList() + (31..38).toList() + (41..48).toList()
            }.map { it.toString() }
            
            val regionList = listOf(
                "Upper left region",
                "Upper right region",
                "Lower left region",
                "Lower right region"
            )
            
            if (toothOptions.isEmpty()) {
                listOf("") + regionList
            } else {
                listOf("") + toothOptions + regionList
            }
        }
        
        DropdownSelector(
            label = "Region",
            options = regionOptions,
            selectedOption = state.patientInfo.chiefComplaintRegion,
            onSelected = { stateHolder.updatePatientInfo(state.patientInfo.copy(chiefComplaintRegion = it)) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Since Date Input
        OutlinedTextField(
            value = state.patientInfo.chiefComplaintSince,
            onValueChange = { stateHolder.updatePatientInfo(state.patientInfo.copy(chiefComplaintSince = it)) },
            label = { Text("Since (Date)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., 2024-01-15 or 2 weeks ago") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Medical Status", style = MaterialTheme.typography.headlineSmall)

        DropdownSelector("Smoking Status", listOf("Never smoker", "Former smoker", "Light smoker", "Heavy smoker"), state.patientInfo.smokingStatus) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(smokingStatus = it))
        }
        DropdownSelector("Diabetic Status", listOf("Normal", "Prediabetic", "Controlled diabetic", "Poorly controlled"), state.patientInfo.diabeticStatus) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(diabeticStatus = it))
        }
        DropdownSelector("Oral Hygiene", listOf("Good", "Fair", "Poor"), state.patientInfo.oralHygiene) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(oralHygiene = it))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = state.patientInfo.otherSystemicDisease,
            onValueChange = { stateHolder.updatePatientInfo(state.patientInfo.copy(otherSystemicDisease = it)) },
            label = { Text("Any Other Systemic Disease") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            placeholder = { Text("e.g., Hypertension, Heart disease, etc.") }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Intraoral Examination", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        ExpandableSectionHeader(
            title = "Tooth",
            expanded = toothSectionExpanded,
            onToggle = { toothSectionExpanded = !toothSectionExpanded }
        )
        
        if (toothSectionExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            DropdownSelector("Matrix Formation",
            options = listOf(
                "0 = Normal matrix formation",
                "-1 = Mild hypoplasia",
                "-2 = Moderate hypoplasia",
                "-3 = Severe hypoplasia",
                "-4 = Amelogenesis Imperfecta",
                "-5 = Dentinogenesis Imperfecta",
                "-6 = Dentin Dysplasia",
                "-7 = Regional Odontodysplasia",
                "-8 = Environmental defects"
            ), 
            selectedOption = state.patientInfo.matrixFormation) { 
            stateHolder.updatePatientInfo(state.patientInfo.copy(matrixFormation = it))
        }
        DropdownSelector("Mineralization",
            options = listOf(
                "0.7-1.0 = Normal mineralization",
                ">1.5 = Severe fluorosis",
                "1.3-1.5 = Moderate fluorosis",
                "1.1-1.3 = Mild fluorosis",
                "1.0-1.1 = Slight hypermineralization",
                "0.5-0.7 = Mild hypomineralization",
                "0.4-0.5 = Moderate hypomineralization",
                "0.2-0.4 = Severe hypomineralization",
                "<0.2 = Extreme hypo (MIH)"
            ),
            selectedOption = state.patientInfo.mineralization) { 
            stateHolder.updatePatientInfo(state.patientInfo.copy(mineralization = it))
        }
        DropdownSelector("Number Status", listOf("0 = Normal dentition", "+1 = Single supernumerary", "+2 = Two supernumerary teeth", "+3 = Multiple supernumerary (≥3)", "-1 = Single missing tooth", "-2 = Multiple missing (2-5)", "-3 = Severe hypodontia (6+)", "-4 = Complete absence (anodontia)"), state.patientInfo.numberStatus) { 
            stateHolder.updatePatientInfo(state.patientInfo.copy(numberStatus = it))
        }
        
        if (toothNumbers.isNotEmpty()) {
            DropdownSelector(
                label = "Select Tooth",
                options = toothNumbers.map { it.toString() },
                selectedOption = selectedToothNumber.toString(),
                onSelected = { selectedToothNumber = it.toInt() }
            )
        } else {
            Text(
                "No teeth available for Edentulous dentition",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (toothNumbers.isNotEmpty()) {
        state.teeth[selectedToothNumber]?.let { tooth ->
            Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Tooth #$selectedToothNumber",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
            // Show status dropdown - default to "normal" if status is not one of the options
            val statusOptions = listOf("normal", "not examined", "abnormal", "missing", "treated")
            val currentStatus = if (tooth.status in statusOptions) tooth.status else "normal"
            
            DropdownSelector(
                label = "Tooth Status",
                options = statusOptions,
                selectedOption = currentStatus,
                onSelected = { 
                    stateHolder.updateToothStatus(selectedToothNumber, it)
                }
            )

            // Show parameters if status is "abnormal"
            if (currentStatus == "abnormal") {
                            Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Parameters for Tooth #$selectedToothNumber",
                                style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        ToothDetailInputs(stateHolder, tooth)
            }
            
            // Show treatment parameters if status is "treated"
            if (currentStatus == "treated") {
                            Spacer(modifier = Modifier.height(12.dp))
                // Get the latest tooth state to ensure reactivity
                val currentTooth = state.teeth[selectedToothNumber] ?: tooth
                        Text(
                            "Treatment Parameters for Tooth #$selectedToothNumber",
                                style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DropdownSelector(
                            label = "Treatment Type",
                            options = listOf("restoration", "root canal", "implant", "prosthesis"),
                            selectedOption = if (currentTooth.treatmentType.isNotEmpty()) currentTooth.treatmentType else "restoration",
                            onSelected = { 
                                stateHolder.updateTreatmentType(selectedToothNumber, it)
                                // Auto-set default status if not already set
                                val updatedTooth = state.teeth[selectedToothNumber]
                                if (updatedTooth?.treatmentStatus?.isEmpty() == true) {
                                    stateHolder.updateTreatmentStatus(selectedToothNumber, "good")
                                }
                            }
                        )
                        
                        // Show treatment status dropdown only if treatment type is selected
                        if (currentTooth.treatmentType.isNotEmpty()) {
                            DropdownSelector(
                                label = "Treatment Status",
                                options = listOf("good", "failed"),
                                selectedOption = if (currentTooth.treatmentStatus.isNotEmpty()) currentTooth.treatmentStatus else "good",
                                onSelected = { 
                                    stateHolder.updateTreatmentStatus(selectedToothNumber, it)
                                }
                            )
                        }
                    }
                }
            }
        }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ExpandableSectionHeader(
            title = "Periodontal",
            expanded = periodontalSectionExpanded,
            onToggle = { periodontalSectionExpanded = !periodontalSectionExpanded }
        )
        
        if (periodontalSectionExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            DropdownSelector(
            label = "Select Segment",
            options = (1..6).map { it.toString() },
            selectedOption = selectedSegmentNumber.toString(),
            onSelected = { 
                selectedSegmentNumber = it.toInt()
                stateHolder.selectSegment(it.toInt())
            }
        )

        state.gingivalSegments[selectedSegmentNumber]?.let { segment ->
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Parameters for Segment #$selectedSegmentNumber",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GumsDetailInputs(stateHolder, segment)
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ExpandableSectionHeaderSmall(
            title = "Oral Mucosa",
            expanded = oralMucosaSectionExpanded,
            onToggle = { oralMucosaSectionExpanded = !oralMucosaSectionExpanded }
        )
        
        if (oralMucosaSectionExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Lesion Characteristics", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            DropdownSelector("Lesion Type (Primary Morphology)", listOf(
            "-10 = Deep ulcer (>5mm depth, undermined edges)",
            "-9 = Severe ulceration (3-5mm depth, punched out)",
            "-8 = Moderate ulcer (2-3mm depth, clean base)",
            "-7 = Shallow ulcer (1-2mm depth, fibrinous coating)",
            "-6 = Erosion (epithelial loss, no depth)",
            "-5 = Fissure/cleft (linear split)",
            "-4 = Pseudomembrane (removable coating)",
            "-3 = Atrophy (thinned epithelium)",
            "-2 = Depression (subtle concavity)",
            "-1 = Slight flattening",
            "0 = Normal mucosa (smooth, intact)",
            "+1 = Slight elevation",
            "+2 = Papule (≤5mm, solid)",
            "+3 = Nodule (>5mm, solid, palpable depth)",
            "+4 = Plaque (flat-topped elevation, >1cm)",
            "+5 = Vesicle (≤5mm, fluid-filled)",
            "+6 = Bulla (>5mm, fluid-filled)",
            "+7 = Pustule (pus-filled)",
            "+8 = Granular/papillary",
            "+9 = Verrucous (warty surface)",
            "+10 = Exophytic mass (fungating growth)"
        ), state.patientInfo.lesionType) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lesionType = it))
        }
        
        DropdownSelector("Size (Maximum Dimension in mm)", listOf(
            "-10 = <1mm (microscopic)",
            "-8 = 1-2mm",
            "-6 = 3-4mm",
            "-4 = 5-6mm",
            "-2 = 7-9mm",
            "0 = 10mm (1cm)",
            "+2 = 11-15mm",
            "+4 = 16-20mm",
            "+6 = 21-30mm",
            "+8 = 31-50mm",
            "+10 = >50mm"
        ), state.patientInfo.lesionSize) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lesionSize = it))
        }
        
        DropdownSelector("Color", listOf(
            "-10 = Completely white/depigmented",
            "-9 = Chalky white (leukoplakia)",
            "-8 = Opaque white",
            "-7 = Translucent white",
            "-6 = White-yellow (necrotic)",
            "-5 = Pale pink (anemic)",
            "-4 = Light pink",
            "-3 = Slightly lighter than normal",
            "-2 = Subtle pallor",
            "-1 = Barely lighter",
            "0 = Normal mucosa color (coral pink)",
            "+1 = Slightly darker/pinker",
            "+2 = Mild erythema (light red)",
            "+3 = Moderate erythema (red)",
            "+4 = Bright red (acute inflammation)",
            "+5 = Deep red (intense inflammation)",
            "+6 = Purple/violaceous",
            "+7 = Blue-black (hematoma)",
            "+8 = Brown (melanotic)",
            "+9 = Gray-black (amalgam tattoo)",
            "+10 = Jet black (melanoma/necrosis)"
        ), state.patientInfo.lesionColor) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lesionColor = it))
        }
        
        DropdownSelector("Surface Texture", listOf(
            "-10 = Glassy smooth (severe atrophy)",
            "-8 = Very smooth (atrophic)",
            "-6 = Smooth (loss of texture)",
            "-4 = Slightly smooth",
            "-2 = Minimal smoothing",
            "0 = Normal texture",
            "+2 = Slightly rough",
            "+4 = Granular",
            "+6 = Papillary",
            "+8 = Verrucous",
            "+10 = Severely irregular (cauliflower-like)"
        ), state.patientInfo.surfaceTexture) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(surfaceTexture = it))
        }
        
        DropdownSelector("Border Definition", listOf(
            "-10 = No discernible border (diffuse)",
            "-8 = Very indistinct",
            "-6 = Indistinct",
            "-4 = Somewhat indistinct",
            "-2 = Slightly indistinct",
            "0 = Moderately defined",
            "+2 = Fairly well-defined",
            "+4 = Well-defined",
            "+6 = Sharply defined",
            "+8 = Very sharp border",
            "+10 = Extremely sharp (rolled border)"
        ), state.patientInfo.borderDefinition) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(borderDefinition = it))
        }
        
        DropdownSelector("Induration (Palpable Firmness)", listOf(
            "-10 = Extremely soft/fluctuant (abscess)",
            "-8 = Very soft (fluid-filled)",
            "-6 = Soft (spongy, edematous)",
            "-4 = Somewhat soft",
            "-2 = Slightly soft",
            "0 = Normal consistency",
            "+2 = Slightly firm",
            "+4 = Firm",
            "+6 = Indurated (hard base)",
            "+8 = Very hard (malignancy sign)",
            "+10 = Stone-hard (invasive carcinoma)"
        ), state.patientInfo.induration) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(induration = it))
        }
        
        DropdownSelector("Pain/Tenderness", listOf(
            "0 = No pain (asymptomatic)",
            "+1 = Minimal discomfort",
            "+2 = Mild pain",
            "+3 = Mild-moderate",
            "+4 = Moderate pain",
            "+5 = Moderate-severe",
            "+6 = Severe pain",
            "+7 = Very severe",
            "+8 = Intense pain",
            "+9 = Excruciating",
            "+10 = Worst imaginable pain"
        ), state.patientInfo.lesionPain) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lesionPain = it))
        }
        
        DropdownSelector("Burning Sensation", listOf(
            "0 = No burning",
            "+1 = Barely perceptible tingling",
            "+2 = Mild tingling/warmth",
            "+3 = Noticeable burning (intermittent)",
            "+4 = Moderate burning (frequent)",
            "+5 = Moderate-severe",
            "+6 = Severe burning",
            "+7 = Very severe",
            "+8 = Intense burning",
            "+9 = Unbearable burning",
            "+10 = Extreme burning"
        ), state.patientInfo.burningSensation) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(burningSensation = it))
        }
        
        DropdownSelector("Bleeding Tendency", listOf(
            "0 = No bleeding",
            "+1 = Trace bleeding on firm pressure",
            "+2 = Slight bleeding on moderate pressure",
            "+3 = Bleeds on gentle touch",
            "+4 = Spontaneous oozing (occasional)",
            "+5 = Spontaneous bleeding (frequent)",
            "+6 = Easy bleeding on minimal trauma",
            "+7 = Persistent bleeding",
            "+8 = Profuse bleeding",
            "+9 = Severe hemorrhage",
            "+10 = Life-threatening bleeding"
        ), state.patientInfo.bleedingTendency) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(bleedingTendency = it))
        }
        
        DropdownSelector("Duration (Time Since Onset)", listOf(
            "-10 = <24 hours (acute)",
            "-8 = 1-3 days",
            "-6 = 4-7 days",
            "-4 = 8-14 days",
            "-2 = 15-30 days",
            "0 = 1-2 months",
            "+2 = 2-3 months",
            "+4 = 3-6 months",
            "+6 = 6-12 months",
            "+8 = 1-2 years",
            "+10 = >2 years (chronic)"
        ), state.patientInfo.lesionDuration) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lesionDuration = it))
        }
        
        DropdownSelector("Pattern/Distribution", listOf(
            "-10 = Diffuse (entire oral cavity)",
            "-8 = Generalized (multiple sites, bilateral)",
            "-6 = Multifocal (3+ sites, random)",
            "-4 = Bilateral (mirror sites, symmetric)",
            "-2 = Unilateral (one side only)",
            "0 = Solitary (single isolated lesion)",
            "+2 = Grouped (clustered, herpetiform)",
            "+4 = Linear (along a line)",
            "+6 = Segmental (dermatome/zone)",
            "+8 = Reticular (lace-like network)",
            "+10 = Annular/target (ring-shaped)"
        ), state.patientInfo.patternDistribution) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(patternDistribution = it))
        }
        
        DropdownSelector("Mobility/Fixation", listOf(
            "-10 = Freely mobile (pedunculated)",
            "-8 = Very mobile",
            "-6 = Mobile",
            "-4 = Somewhat mobile",
            "-2 = Slightly mobile",
            "0 = Normal mobility",
            "+2 = Slightly adherent",
            "+4 = Adherent to underlying tissue",
            "+6 = Fixed (does not move)",
            "+8 = Firmly fixed (tethered)",
            "+10 = Completely fixed (infiltrating bone)"
        ), state.patientInfo.mobilityFixation) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mobilityFixation = it))
        }
        
        DropdownSelector("Lymphadenopathy", listOf(
            "0 = No palpable nodes",
            "+1 = Barely palpable (<5mm, soft, mobile)",
            "+2 = Small nodes (5-10mm, soft, mobile)",
            "+3 = Moderate nodes (10-15mm, soft, mobile)",
            "+4 = Moderate nodes (10-15mm, firm, mobile)",
            "+5 = Large nodes (15-20mm, firm, mobile)",
            "+6 = Large nodes (15-20mm, hard, mobile)",
            "+7 = Very large nodes (20-30mm, hard, mobile)",
            "+8 = Very large nodes (>30mm, hard, partially fixed)",
            "+9 = Matted nodes (multiple fused, fixed)",
            "+10 = Extensive adenopathy (metastasis)"
        ), state.patientInfo.lymphadenopathy) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lymphadenopathy = it))
        }
        
        DropdownSelector("Associated Systemic Symptoms", listOf(
            "0 = No systemic symptoms",
            "+1 = Minimal (mild fever/malaise)",
            "+2 = Moderate (fever, skin lesions)",
            "+3 = Significant (weight loss, joint pain)",
            "+4 = Severe (multiple system involvement)",
            "+5 = Life-threatening (pemphigus vulgaris)"
        ), state.patientInfo.associatedSystemicSymptoms) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(associatedSystemicSymptoms = it))
        }
        
        DropdownSelector("Removability (of Surface Material)", listOf(
            "-10 = Completely adherent (true keratosis)",
            "-8 = Firmly adherent (scraping bleeds)",
            "-6 = Moderately adherent",
            "-4 = Somewhat adherent",
            "-2 = Slightly adherent",
            "0 = Not applicable (no coating)",
            "+2 = Partially removable",
            "+4 = Mostly removable",
            "+6 = Easily removable (candidiasis)",
            "+8 = Very easily removable",
            "+10 = Wipes off completely"
        ), state.patientInfo.removability) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(removability = it))
        }
        
        DropdownSelector("Growth Rate", listOf(
            "-10 = Rapid regression (>5mm/week decrease)",
            "-8 = Moderate regression (2-5mm/week)",
            "-6 = Slow regression (0.5-2mm/week)",
            "-4 = Minimal regression",
            "-2 = Very slow regression",
            "0 = Stable (no change)",
            "+2 = Very slow growth (<0.5mm/week)",
            "+4 = Slow growth (0.5-1mm/week)",
            "+6 = Moderate growth (1-2mm/week)",
            "+8 = Rapid growth (2-5mm/week - malignancy)",
            "+10 = Very rapid growth (>5mm/week)"
        ), state.patientInfo.growthRate) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(growthRate = it))
        }
        
        DropdownSelector("Nikolsky Sign", listOf(
            "-10 = Not applicable",
            "0 = Negative (epithelium intact)",
            "+2 = Equivocal",
            "+4 = Positive (moderate pressure)",
            "+6 = Strongly positive (slides easily)",
            "+8 = Very positive (large denudation)",
            "+10 = Extremely positive (pemphigus)"
        ), state.patientInfo.nikolskySign) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(nikolskySign = it))
        }
        
        DropdownSelector("Wickham's Striae", listOf(
            "0 = Absent (no white lines)",
            "+2 = Questionable (faint lines)",
            "+4 = Present (visible reticular pattern)",
            "+6 = Prominent (extensive network)",
            "+8 = Very prominent (large area)",
            "+10 = Extensive (classic lichen planus)"
        ), state.patientInfo.wickhamsStriae) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(wickhamsStriae = it))
        }
        
        DropdownSelector("Recurrence Pattern", listOf(
            "-10 = First occurrence, never before",
            "-8 = First occurrence (high suspicion)",
            "-6 = Second occurrence (1 recurrence)",
            "-4 = Occasional (2-3 episodes/year)",
            "-2 = Infrequent (1-2 episodes/year)",
            "0 = No recurrence data",
            "+2 = Frequent (4-6 episodes/year)",
            "+4 = Very frequent (6-10 episodes/year)",
            "+6 = Constant recurrence (>10/year)",
            "+8 = Persistent (never resolves)",
            "+10 = Continuous (always present)"
        ), state.patientInfo.recurrencePattern) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(recurrencePattern = it))
        }
        
        DropdownSelector("Functional Impairment", listOf(
            "0 = No impairment",
            "+1 = Minimal awareness",
            "+2 = Mild impairment",
            "+3 = Mild-moderate",
            "+4 = Moderate (interferes with eating)",
            "+5 = Moderate-severe (limits diet)",
            "+6 = Severe (difficulty eating most foods)",
            "+7 = Very severe (soft diet, speech affected)",
            "+8 = Extreme (liquids only)",
            "+9 = Critical (unable to eat/swallow)",
            "+10 = Complete impairment (NPO status)"
        ), state.patientInfo.functionalImpairment) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(functionalImpairment = it))
        }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ExpandableSectionHeaderSmall(
            title = "Malocclusion",
            expanded = malocclusionSectionExpanded,
            onToggle = { malocclusionSectionExpanded = !malocclusionSectionExpanded }
        )
        
        if (malocclusionSectionExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Extra-Oral Examination", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            
            DropdownSelector("Shape of Head", listOf("+1 = Brachycephalic", "0 = Mesocephalic", "-1 = Dolichocephalic"), state.patientInfo.shapeOfHead) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(shapeOfHead = it))
        }
        DropdownSelector("Facial Symmetry", listOf("0 = Symmetric", "+1 = Mild asymmetry (<2mm)", "+2 = Moderate asymmetry (2-4mm)", "+3 = Severe asymmetry (>4mm)"), state.patientInfo.facialSymmetry) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(facialSymmetry = it))
        }
        DropdownSelector("Facial Form", listOf("+3 = Extremely convex", "+2 = Convex", "+1 = Mildly convex", "0 = Straight", "-1 = Mildly concave", "-2 = Concave", "-3 = Extremely concave"), state.patientInfo.facialForm) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(facialForm = it))
        }
        DropdownSelector("Facial Divergence", listOf("+2 = Anterior divergence (vertical grower)", "0 = Normal", "-2 = Posterior divergence (horizontal grower)"), state.patientInfo.facialDivergence) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(facialDivergence = it))
        }
        DropdownSelector("Interlabial Gap at Rest", listOf("0 = Competent (lips seal)", "+1 = Mild incompetence (strain to seal)", "+2 = Moderate incompetence (2-4mm gap)", "+3 = Severe incompetence (4-6mm)", "+4 = Extreme incompetence (>6mm)"), state.patientInfo.interlabialGapAtRest) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(interlabialGapAtRest = it))
        }
        DropdownSelector("Nasolabial Angle", listOf("+3 = Extremely obtuse (>130°)", "+2 = Obtuse (115-130°)", "+1 = Mildly obtuse (105-115°)", "0 = Normal (90-105°)", "-1 = Mildly acute (80-90°)", "-2 = Acute (70-80°)", "-3 = Extremely acute (<70°)"), state.patientInfo.nasolabialAngle) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(nasolabialAngle = it))
        }
        DropdownSelector("Upper Lip Posture", listOf("+2 = Everted", "+1 = Normal", "0 = Normal length", "-1 = Short"), state.patientInfo.upperLipPosture) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(upperLipPosture = it))
        }
        DropdownSelector("Lower Lip Posture", listOf("+2 = Everted", "+1 = Normal", "0 = Normal length", "-1 = Short", "-2 = Lip trap"), state.patientInfo.lowerLipPosture) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lowerLipPosture = it))
        }
        DropdownSelector("Lip Tonicity", listOf("+2 = Hyperactive (mentalis strain)", "0 = Normal", "-2 = Hypoactive"), state.patientInfo.lipTonicity) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lipTonicity = it))
        }
        DropdownSelector("Resting Lower Lip Line", listOf("+2 = High (gingival show >3mm)", "+1 = Above average (1-3mm)", "0 = Average (at gingival margin)", "-1 = Low (covers >2mm)", "-2 = Very low (covers >4mm)"), state.patientInfo.restingLowerLipLine) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(restingLowerLipLine = it))
        }
        DropdownSelector("Mentolabial Sulcus", listOf("+2 = Very shallow (<1mm)", "+1 = Shallow (1-2mm)", "0 = Average (3-5mm)", "-1 = Deep (5-7mm)", "-2 = Very deep (>7mm)"), state.patientInfo.mentolabialSulcus) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mentolabialSulcus = it))
        }
        DropdownSelector("Chin Position", listOf("+3 = Severely protruding", "+2 = Moderately protruding", "+1 = Mildly protruding", "0 = Normal", "-1 = Mildly receding", "-2 = Moderately receding", "-3 = Severely receding"), state.patientInfo.chinPosition) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(chinPosition = it))
        }
        DropdownSelector("AP Skeletal Relationship", listOf("+3 = Severe skeletal Class III", "+2 = Moderate skeletal Class III", "+1 = Mild skeletal Class III", "0 = Skeletal Class I", "-1 = Mild skeletal Class II", "-2 = Moderate skeletal Class II", "-3 = Severe skeletal Class II"), state.patientInfo.apSkeletalRelationship) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(apSkeletalRelationship = it))
        }
        DropdownSelector("Total Face Height", listOf("+3 = Severely increased", "+2 = Moderately increased", "+1 = Mildly increased", "0 = Normal", "-1 = Mildly decreased", "-2 = Moderately decreased", "-3 = Severely decreased"), state.patientInfo.totalFaceHeight) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(totalFaceHeight = it))
        }
        DropdownSelector("Upper Face Height", listOf("+2 = Increased", "0 = Normal", "-2 = Decreased"), state.patientInfo.upperFaceHeight) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(upperFaceHeight = it))
        }
        DropdownSelector("Middle Face Height", listOf("+2 = Increased", "0 = Normal", "-2 = Decreased"), state.patientInfo.middleFaceHeight) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(middleFaceHeight = it))
        }
        DropdownSelector("Lower Face Height", listOf("+3 = Severely increased (>60%)", "+2 = Moderately increased (57-60%)", "+1 = Mildly increased (55-57%)", "0 = Normal (50-55%)", "-1 = Mildly decreased (47-50%)", "-2 = Moderately decreased (45-47%)", "-3 = Severely decreased (<45%)"), state.patientInfo.lowerFaceHeight) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lowerFaceHeight = it))
        }
        DropdownSelector("VTO (Visual Treatment Objective)", listOf("+2 = Positive (good improvement expected)", "0 = Neutral", "-2 = Negative (surgery needed)"), state.patientInfo.vto) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(vto = it))
        }
        DropdownSelector("Clinical FMPA (Facial Axis)", listOf("+3 = Severe hyperdivergent (vertical)", "+2 = Moderate hyperdivergent", "+1 = Mild hyperdivergent", "0 = Average growth pattern", "-1 = Mild hypodivergent", "-2 = Moderate hypodivergent (horizontal)", "-3 = Severe hypodivergent"), state.patientInfo.clinicalFmpa) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(clinicalFmpa = it))
        }
        DropdownSelector("Incisor Exposure at Rest", listOf("+3 = Excessive (>4mm)", "+2 = Increased (3-4mm)", "+1 = Slightly increased (2-3mm)", "0 = Normal (1-2mm)", "-1 = Decreased (<1mm)", "-2 = None"), state.patientInfo.incisorExposureAtRest) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(incisorExposureAtRest = it))
        }
        DropdownSelector("Incisor Exposure at Speech", listOf("+2 = Increased", "0 = Normal", "-2 = Decreased"), state.patientInfo.incisorExposureAtSpeech) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(incisorExposureAtSpeech = it))
        }
        DropdownSelector("Incisor Exposure on Smiling", listOf("+3 = Excessive gingival show (>4mm)", "+2 = Increased gingival show (2-4mm)", "+1 = Mild gingival show (1-2mm)", "0 = Normal (0-1mm)", "-1 = Reduced tooth show", "-2 = Minimal tooth show"), state.patientInfo.incisorExposureOnSmiling) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(incisorExposureOnSmiling = it))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Functional Examination", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        DropdownSelector("Respiratory Pattern", listOf("0 = Nasal breathing", "+1 = Oro-nasal mixed", "+2 = Predominantly oral", "+3 = Obligate oral breathing"), state.patientInfo.respiratoryPattern) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(respiratoryPattern = it))
        }
        DropdownSelector("Deglutition", listOf("0 = Normal mature swallow", "+1 = Transitional", "+2 = Infantile pattern", "+3 = Severe tongue thrust"), state.patientInfo.deglutition) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(deglutition = it))
        }
        DropdownSelector("Mastication", listOf("0 = Normal bilateral", "+1 = Predominantly unilateral", "+2 = Unilateral only", "+3 = Abnormal with compensations"), state.patientInfo.mastication) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mastication = it))
        }
        DropdownSelector("Speech", listOf("0 = Normal articulation", "+1 = Mild issues", "+2 = Moderate issues", "+3 = Severe impediment"), state.patientInfo.speech) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(speech = it))
        }
        DropdownSelector("Path of Closure", listOf("0 = Normal", "+1 = Forward shift", "+2 = Backward shift", "+3 = Complex deviation"), state.patientInfo.pathOfClosure) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(pathOfClosure = it))
        }
        DropdownSelector("Lateral Path of Closure", listOf("0 = Normal", "+1 = Mild deviation (<1mm)", "+2 = Moderate deviation (1-2mm)", "+3 = Deflection with shift (>2mm)"), state.patientInfo.lateralPathOfClosure) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lateralPathOfClosure = it))
        }
        DropdownSelector("TMJ Function", listOf("0 = Normal", "+1 = Clicking only", "+2 = Clicking with pain", "+3 = Pain with limitation", "+4 = Locking", "+5 = Severe dysfunction"), state.patientInfo.tmjFunction) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(tmjFunction = it))
        }
        DropdownSelector("Postural Rest Position of Tongue", listOf("0 = Normal (against palate)", "+1 = Slightly low", "+2 = Low posture", "+3 = Severely low/forward"), state.patientInfo.posturalRestPositionOfTongue) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(posturalRestPositionOfTongue = it))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Maxillary Arch Parameters", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        DropdownSelector("Maxillary Arch Shape", listOf("+2 = Square", "0 = Average/U-shaped", "-2 = Tapered", "-3 = Severely constricted"), state.patientInfo.maxillaryArchShape) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryArchShape = it))
        }
        DropdownSelector("Maxillary Arch Symmetry", listOf("0 = Symmetric", "+1 = Mild asymmetry", "+2 = Moderate asymmetry", "+3 = Severe asymmetry"), state.patientInfo.maxillaryArchSymmetry) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryArchSymmetry = it))
        }
        DropdownSelector("Maxillary Crowding", listOf("0 = No crowding (≥0mm)", "-1 = Mild (<4mm)", "-2 = Moderate (4-8mm)", "-3 = Severe (>8mm)"), state.patientInfo.maxillaryCrowding) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryCrowding = it))
        }
        DropdownSelector("Maxillary Spacing", listOf("0 = No spacing", "+1 = Mild (<4mm)", "+2 = Moderate (4-8mm)", "+3 = Severe (>8mm)"), state.patientInfo.maxillarySpacing) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillarySpacing = it))
        }
        DropdownSelector("Maxillary Rotation", listOf("0 = No rotation", "+1 = Mild (<15°)", "+2 = Moderate (15-45°)", "+3 = Severe (>45°)"), state.patientInfo.maxillaryRotation) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryRotation = it))
        }
        DropdownSelector("Maxillary Axial Inclination", listOf("0 = Normal", "+1 = Mild tipping", "+2 = Moderate tipping", "+3 = Severe tipping"), state.patientInfo.maxillaryAxialInclination) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryAxialInclination = it))
        }
        DropdownSelector("Maxillary Transposition", listOf("0 = No transposition", "+1 = Partial transposition", "+2 = Complete transposition"), state.patientInfo.maxillaryTransposition) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maxillaryTransposition = it))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Mandibular Arch Parameters", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        DropdownSelector("Mandibular Arch Shape", listOf("+2 = Square", "0 = Average/U-shaped", "-2 = Tapered", "-3 = Severely constricted"), state.patientInfo.mandibularArchShape) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularArchShape = it))
        }
        DropdownSelector("Mandibular Arch Symmetry", listOf("0 = Symmetric", "+1 = Mild asymmetry", "+2 = Moderate asymmetry", "+3 = Severe asymmetry"), state.patientInfo.mandibularArchSymmetry) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularArchSymmetry = it))
        }
        DropdownSelector("Mandibular Crowding", listOf("0 = No crowding (≥0mm)", "-1 = Mild (<4mm)", "-2 = Moderate (4-8mm)", "-3 = Severe (>8mm)"), state.patientInfo.mandibularCrowding) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularCrowding = it))
        }
        DropdownSelector("Mandibular Spacing", listOf("0 = No spacing", "+1 = Mild (<4mm)", "+2 = Moderate (4-8mm)", "+3 = Severe (>8mm)"), state.patientInfo.mandibularSpacing) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularSpacing = it))
        }
        DropdownSelector("Mandibular Rotation", listOf("0 = No rotation", "+1 = Mild (<15°)", "+2 = Moderate (15-45°)", "+3 = Severe (>45°)"), state.patientInfo.mandibularRotation) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularRotation = it))
        }
        DropdownSelector("Mandibular Axial Inclination", listOf("0 = Normal", "+1 = Mild tipping", "+2 = Moderate tipping", "+3 = Severe tipping"), state.patientInfo.mandibularAxialInclination) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularAxialInclination = it))
        }
        DropdownSelector("Mandibular Transposition", listOf("0 = No transposition", "+1 = Partial transposition", "+2 = Complete transposition"), state.patientInfo.mandibularTransposition) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(mandibularTransposition = it))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Inter-Arch Relationship Parameters", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        DropdownSelector("Maximum Mouth Opening", listOf("+2 = Hypermobile (>50mm)", "+1 = Increased (45-50mm)", "0 = Normal (35-45mm)", "-1 = Reduced (25-35mm)", "-2 = Severely limited (<25mm)"), state.patientInfo.maximumMouthOpening) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(maximumMouthOpening = it))
        }
        DropdownSelector("Freeway Space", listOf("+2 = Excessive (>5mm)", "+1 = Increased (4-5mm)", "0 = Normal (2-4mm)", "-1 = Reduced (1-2mm)", "-2 = Minimal (<1mm)"), state.patientInfo.freewaySpace) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(freewaySpace = it))
        }
        DropdownSelector("Curve of Spee", listOf("+3 = Severely exaggerated (>4mm)", "+2 = Moderately exaggerated (3-4mm)", "+1 = Mildly exaggerated (2-3mm)", "0 = Normal/Flat (0-2mm)", "-1 = Reverse curve"), state.patientInfo.curveOfSpee) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(curveOfSpee = it))
        }
        DropdownSelector("Molar Relationship (Angle's Classification)", listOf("+3 = Full Class III (>4mm mesial)", "+2 = End-to-end Class III", "+1 = Mild Class III tendency", "0 = Class I (normal)", "-1 = Mild Class II tendency", "-2 = End-to-end Class II", "-3 = Full Class II Division 1", "-4 = Full Class II Division 2"), state.patientInfo.molarRelationship) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(molarRelationship = it))
        }
        DropdownSelector("Canine Relationship", listOf("+2 = Class III canine", "+1 = End-to-end Class III", "0 = Class I canine", "-1 = End-to-end Class II", "-2 = Class II canine"), state.patientInfo.canineRelationship) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(canineRelationship = it))
        }
        DropdownSelector("Incisor Relationship", listOf("+3 = Severe anterior crossbite", "+2 = Moderate anterior crossbite", "+1 = Edge-to-edge", "0 = Class I normal", "-1 = Class II Division 2 pattern", "-2 = Class II Division 1 mild", "-3 = Class II Division 1 moderate", "-4 = Class II Division 1 severe"), state.patientInfo.incisorRelationship) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(incisorRelationship = it))
        }
        DropdownSelector("Overjet (Horizontal Overlap in mm)", listOf("+4 = Reverse overjet severe (>5mm)", "+3 = Reverse overjet moderate (3-5mm)", "+2 = Reverse overjet mild (1-3mm)", "+1 = Edge-to-edge (0-1mm)", "0 = Normal (2-3mm)", "-1 = Increased (4-6mm)", "-2 = Severe increased (6-8mm)", "-3 = Extreme increased (8-10mm)", "-4 = Very extreme (>10mm)"), state.patientInfo.overjet) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(overjet = it))
        }
        DropdownSelector("Overbite (Vertical Overlap)", listOf("+4 = Severe open bite (>5mm)", "+3 = Moderate open bite (3-5mm)", "+2 = Mild open bite (1-3mm)", "+1 = Reduced overbite (10-20%)", "0 = Normal (20-40%)", "-1 = Increased (40-60%)", "-2 = Deep bite (60-80%)", "-3 = Severe deep bite (80-100%)", "-4 = Complete deep bite (>100%)", "-5 = True deep bite (impinging palate)", "-6 = Pseudo deep bite (gingival impingement)"), state.patientInfo.overbite) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(overbite = it))
        }
        DropdownSelector("Crossbite Type", listOf("0 = No crossbite", "+1 = Anterior crossbite (1-2 teeth)", "+2 = Anterior crossbite (3-4 teeth)", "+3 = Anterior crossbite (>4 teeth)", "+4 = Unilateral posterior crossbite", "+5 = Bilateral posterior crossbite", "+6 = Scissors bite", "+7 = Complete crossbite (all teeth)"), state.patientInfo.crossbiteType) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(crossbiteType = it))
        }
        DropdownSelector("Upper Dental Midline", listOf("0 = Coincident with facial midline", "+1 = Deviated right (1-2mm)", "+2 = Deviated right (2-3mm)", "+3 = Deviated right (>3mm)", "-1 = Deviated left (1-2mm)", "-2 = Deviated left (2-3mm)", "-3 = Deviated left (>3mm)"), state.patientInfo.upperDentalMidline) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(upperDentalMidline = it))
        }
        DropdownSelector("Lower Dental Midline", listOf("0 = Coincident with facial midline", "+1 = Deviated right (1-2mm)", "+2 = Deviated right (2-3mm)", "+3 = Deviated right (>3mm)", "-1 = Deviated left (1-2mm)", "-2 = Deviated left (2-3mm)", "-3 = Deviated left (>3mm)"), state.patientInfo.lowerDentalMidline) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(lowerDentalMidline = it))
        }
        DropdownSelector("Inter-Arch Midline Discrepancy", listOf("0 = Coincident midlines", "+1 = Mild discrepancy (1-2mm)", "+2 = Moderate discrepancy (2-4mm)", "+3 = Severe discrepancy (>4mm)"), state.patientInfo.interArchMidlineDiscrepancy) {
            stateHolder.updatePatientInfo(state.patientInfo.copy(interArchMidlineDiscrepancy = it))
        }
        }
    }
}

@Composable
fun GumsDetailInputs(stateHolder: DentalStateHolder, segment: GingivalSegment) {
    Column {
        Text("Clinical Gingival Examination", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Size/Edema", listOf("Normal", "Mild", "Moderate", "Marked", "Recession"), segment.sizeEdema) { stateHolder.updateGingivalSizeEdema(segment.number, it) }
        DropdownSelector("Color", listOf("Pink", "Reddish-pink", "Red", "Dark red/cyanotic", "Mixed", "Necrotic"), segment.color) { stateHolder.updateGingivalColor(segment.number, it) }
        DropdownSelector("Shape", listOf("Normal (scalloped)", "Rounded", "Irregular", "Bulbous", "Rolled", "Nodular", "Cratered"), segment.shape) { stateHolder.updateGingivalShape(segment.number, it) }
        DropdownSelector("Contour", listOf("Normal (pointed)", "Blunted", "Flattened", "Enlarged", "Irregular", "Clefted"), segment.contour) { stateHolder.updateGingivalContour(segment.number, it) }
        DropdownSelector("Texture", listOf("Stippled (normal)", "Loss of stippling", "Smooth/shiny", "Fibrotic/leathery", "Ulcerated"), segment.texture) { stateHolder.updateGingivalTexture(segment.number, it) }
        DropdownSelector("Consistency", listOf("Firm and resilient", "Spongy", "Soft/edematous", "Hyperkeratotic", "Friable", "Retractile"), segment.consistency) { stateHolder.updateGingivalConsistency(segment.number, it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Inflammation & Distribution", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Distribution", listOf("None", "Papillary", "Marginal", "Diffuse"), segment.distribution) { stateHolder.updateGingivalDistribution(segment.number, it) }
        DropdownSelector("Bleeding", listOf("No", "Yes (spot)", "Yes (profuse)"), segment.bleeding) { stateHolder.updateGingivalBleeding(segment.number, it) }
        DropdownSelector("Exudate", listOf("None", "Present on pressure"), segment.exudate) { stateHolder.updateGingivalExudate(segment.number, it) }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deposits & Plaque Assessment", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Calculus", listOf("None", "Supragingival", "Subgingival", "Heavy"), segment.calculus) { stateHolder.updateGingivalCalculus(segment.number, it) }
        DropdownSelector("Plaque", listOf("None", "Light", "Moderate", "Abundant"), segment.plaque) { stateHolder.updateGingivalPlaque(segment.number, it) }
    }
}

@Composable
fun ToothDetailInputs(stateHolder: DentalStateHolder, tooth: Tooth) {
    Column {
        Text("Developmental Stage Parameters", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Tooth Size", listOf("0 = Normal (±1.5mm)", "+1 to +10 = Macrodontia (varying degrees)", "-1 to -10 = Microdontia (varying degrees)"), tooth.toothSize) { stateHolder.updateToothSize(tooth.number, it) }
        DropdownSelector("Tooth Shape", listOf("0 = Normal shape", "+2 = Twinning (complete division)", "+1 = Gemination (single bud divides)", "-1 = Fusion (two buds fuse)", "-2 = Concrescence (cementum fusion)"), tooth.toothShape) { stateHolder.updateToothShape(tooth.number, it) }
        DropdownSelector("Tooth Morphology", listOf("0 = Normal morphology", "+4 = Talon cusp", "+3 = Lobodontia/Globodontia", "+2 = Taurodontism", "+1 = Dens evaginatus", "-1 = Dens invaginatus Type I", "-2 = Dens invaginatus Type II", "-3 = Dens invaginatus Type III", "-4 = Dilaceration"), tooth.toothMorphology) { stateHolder.updateToothMorphology(tooth.number, it) }
        DropdownSelector("Eruption Status", listOf("0 = Normal eruption", "+2 = Natal teeth", "+1 = Neonatal teeth", "-1 = Submerged (1-2mm below)", "-2 = Retained (2-4mm below)", "-3 = Embedded (>4mm, soft tissue)", "-4 = Impacted (>4mm, bone obstruction)", "-5 = Ectopic eruption"), tooth.eruptionStatus) { stateHolder.updateEruptionStatus(tooth.number, it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tooth Structure & Surface Parameters", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Caries (ICDAS)", listOf("0 = ICDAS 0 - Sound tooth", "+1 = ICDAS 1 - First visual change", "+2 = ICDAS 2 - Distinct visual change", "+3 = ICDAS 3 - Localized enamel breakdown", "+4 = ICDAS 4 - Dark shadow from dentin", "+5 = ICDAS 5 - Cavity with visible dentin (<50%)", "+6 = ICDAS 6 - Extensive cavity (>50%)", "+7 = Root caries - Active", "+8 = Root caries - Cavitated"), tooth.caries) { stateHolder.updateCaries(tooth.number, it) }
        DropdownSelector("Erosion (BEWE)", listOf("0 = No erosive wear", "+1 = Initial loss of surface texture", "+2 = Defect with hard tissue loss <50%", "+3 = Hard tissue loss ≥50%"), tooth.erosion) { stateHolder.updateErosion(tooth.number, it) }
        DropdownSelector("Fracture Type", listOf("0 = No fracture", "+1 = Ellis Class I (enamel only)", "+2 = Ellis Class II (enamel + dentin)", "+3 = Ellis Class III (+ pulp)", "+4 = Crown-root fracture", "+5 = Horizontal root fracture", "+6 = Vertical root fracture", "+7 = Oblique root fracture", "+8 = Comminuted fracture"), tooth.fractureType) { stateHolder.updateFractureType(tooth.number, it) }
        DropdownSelector("Attrition", listOf("0 = No attrition", "+1 = Minimal (enamel facets)", "+2 = Moderate (dentin exposure <1/3)", "+3 = Severe (dentin 1/3-2/3)", "+4 = Extreme (dentin >2/3)"), tooth.attrition) { stateHolder.updateAttrition(tooth.number, it) }
        DropdownSelector("Abrasion", listOf("0 = No abrasion", "+1 = Minimal (V-notch at CEJ, <1mm)", "+2 = Moderate (1-2mm, mild sensitivity)", "+3 = Severe (2-3mm, moderate sensitivity)", "+4 = Extreme (>3mm, severe sensitivity)"), tooth.abrasion) { stateHolder.updateAbrasion(tooth.number, it) }
        DropdownSelector("Abfraction", listOf("0 = No abfraction", "+1 = Minimal (wedge defect <1mm)", "+2 = Moderate (1-2mm, mild sensitivity)", "+3 = Severe (2-3mm, moderate sensitivity)", "+4 = Extreme (>3mm, severe sensitivity)"), tooth.abfraction) { stateHolder.updateAbfraction(tooth.number, it) }
        DropdownSelector("Discoloration Type", listOf("0 = Normal tooth color", "+1 = White spot", "+2 = Yellow (extrinsic stain)", "+3 = Brown (fluorosis, staining)", "+4 = Gray (non-vital, amalgam)", "+5 = Black (severe caries, necrosis)", "+6 = Pink (internal resorption)", "+7 = Tetracycline staining", "+8 = Amelogenesis imperfecta"), tooth.discoloration) { stateHolder.updateDiscoloration(tooth.number, it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Pulp Vitality & Pain Parameters", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Pain Status", listOf("0 = No pain (asymptomatic)", "+1 = Pain on cold/hot (reversible pulpitis)", "+2 = Pain on stimuli only", "+3 = Prolonged pain without stimuli (irreversible)", "+4 = Severe spontaneous throbbing pain", "+5 = No response to stimuli (necrotic pulp)"), tooth.painStatus) { stateHolder.updatePainStatus(tooth.number, it) }
        DropdownSelector("Tender on Percussion", listOf("0 = Negative", "+1 = Positive (periapical inflammation)"), tooth.tenderOnPercussion) { stateHolder.updateTenderOnPercussion(tooth.number, it) }
        DropdownSelector("Vestibular Tenderness", listOf("0 = Negative", "+1 = Positive (acute periapical abscess)"), tooth.vestibularTenderness) { stateHolder.updateVestibularTenderness(tooth.number, it) }
        DropdownSelector("Sinus Tract", listOf("0 = Normal (no sinus tract)", "+1 = Opened (draining sinus tract)", "+2 = Multiple sinus tracts"), tooth.sinusTract) { stateHolder.updateSinusTract(tooth.number, it) }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Periodontal Parameters", style = MaterialTheme.typography.headlineSmall)
        DropdownSelector("Recession", listOf("None", "Class I", "Class II", "Class III", "Class IV"), tooth.recession) { stateHolder.updateRecession(tooth.number, it) }
        DropdownSelector("Pocket", listOf("Normal 1-3mm", "Mild 4-5mm", "Mod 6-7mm", "Severe ≥8mm", "Pseudo-pocket"), tooth.pocket) { stateHolder.updatePocket(tooth.number, it) }
        DropdownSelector("Mobility", listOf("None", "Class I", "Class II", "Class III"), tooth.mobility) { stateHolder.updateMobility(tooth.number, it) }
        DropdownSelector("Furcation", listOf("None", "Class I", "Class II", "Class III", "Class IV"), tooth.furcation) { stateHolder.updateFurcation(tooth.number, it) }
    }
}

@Composable
fun OdontogramScreen(stateHolder: DentalStateHolder, state: DentalState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            OdontogramWebView(
                modifier = Modifier.fillMaxSize(),
                state = state
            )
        }
        
        state.selectedTooth?.let { toothNum ->
            val selectedTooth = state.teeth[toothNum]
            Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tooth $toothNum Status: ${selectedTooth?.status}", style = MaterialTheme.typography.titleMedium)
                    Text("Periodontal Status: ${selectedTooth?.periodontalStatus}")
                    Text("Visual Changes: ${selectedTooth?.visualChanges}")
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(state: DentalState, stateHolder: DentalStateHolder? = null) {
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Assessment Summary", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Chief Complaint Section
        if (state.patientInfo.chiefComplaint.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Chief Complaint",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildString {
                            append("Patient complains of ")
                            append(state.patientInfo.chiefComplaint)
                            if (state.patientInfo.chiefComplaintRegion.isNotEmpty()) {
                                append(" at ${state.patientInfo.chiefComplaintRegion}")
                            }
                            if (state.patientInfo.chiefComplaintSince.isNotEmpty()) {
                                append(" since ${state.patientInfo.chiefComplaintSince}")
                            }
                            append(".")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

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
        
        SummaryItem("Total Teeth Abnormal", relevantTeeth.count { it.status.equals("abnormal", ignoreCase = true) }.toString())
        SummaryItem("Total Teeth Missing", relevantTeeth.count { it.status.equals("missing", ignoreCase = true) }.toString())
        SummaryItem("Total Teeth Treated", relevantTeeth.count { it.status.equals("treated", ignoreCase = true) }.toString())
        SummaryItem("Total Teeth Not Examined", relevantTeeth.count { it.status.equals("not examined", ignoreCase = true) }.toString())
        SummaryItem("Oral Hygiene", state.patientInfo.oralHygiene)
        SummaryItem("Diabetic Status", state.patientInfo.diabeticStatus)

        // --- Treatment Summary ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Treatment Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

        val treatmentTypes = listOf("restoration", "root canal", "implant", "prosthesis")
        
        // Get all treated teeth
        val allTreatedTeeth = state.teeth.values.filter { 
            it.status.equals("treated", ignoreCase = true) 
        }
        
        var treatmentsFound = false
        treatmentTypes.forEach { treatmentType ->
            val treatedTeeth = allTreatedTeeth
                .filter { 
                    it.treatmentType.equals(treatmentType, ignoreCase = true)
                }
                    .map { it.number }
                .sorted()
                
                if (treatedTeeth.isNotEmpty()) {
                val treatmentTypeLabel = treatmentType.replaceFirstChar { it.uppercaseChar() }
                AnomalySummaryItem(treatmentTypeLabel, treatedTeeth.joinToString(", "))
                    treatmentsFound = true
                }
            }
        
        // Also show treated teeth without a specific treatment type or with unrecognized types
        val treatedWithoutType = allTreatedTeeth
            .filter { 
                it.treatmentType.isEmpty() || 
                !treatmentTypes.any { type -> it.treatmentType.equals(type, ignoreCase = true) }
            }
            .map { it.number }
            .sorted()
        
        if (treatedWithoutType.isNotEmpty()) {
            AnomalySummaryItem("Treated (Other)", treatedWithoutType.joinToString(", "))
            treatmentsFound = true
        }
        
                if (!treatmentsFound) {
                    Text(
                        "None detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // --- Developmental Anomalies ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Developmental Anomalies",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

        val developmentalAnomalies = listOf(
            "Macrodontia" to { tooth: Tooth -> tooth.toothSize == "+1 to +10 = Macrodontia (varying degrees)" },
            "Microdontia" to { tooth: Tooth -> tooth.toothSize == "-1 to -10 = Microdontia (varying degrees)" },
            "Twinning" to { tooth: Tooth -> tooth.toothShape == "+2 = Twinning (complete division)" },
            "Gemination" to { tooth: Tooth -> tooth.toothShape == "+1 = Gemination (single bud divides)" },
            "Fusion" to { tooth: Tooth -> tooth.toothShape == "-1 = Fusion (two buds fuse)" },
            "Concrescence" to { tooth: Tooth -> tooth.toothShape == "-2 = Concrescence (cementum fusion)" },
            "Talon Cusp" to { tooth: Tooth -> tooth.toothMorphology == "+4 = Talon cusp" },
            "Lobodontia" to { tooth: Tooth -> tooth.toothMorphology == "+3 = Lobodontia/Globodontia" },
            "Taurodontism" to { tooth: Tooth -> tooth.toothMorphology == "+2 = Taurodontism" },
            "Dens Evaginatus" to { tooth: Tooth -> tooth.toothMorphology == "+1 = Dens evaginatus" },
            "Dens Invaginatus Type I" to { tooth: Tooth -> tooth.toothMorphology == "-1 = Dens invaginatus Type I" },
            "Dens Invaginatus Type II" to { tooth: Tooth -> tooth.toothMorphology == "-2 = Dens invaginatus Type II" },
            "Dens Invaginatus Type III" to { tooth: Tooth -> tooth.toothMorphology == "-3 = Dens invaginatus Type III" },
            "Dilaceration" to { tooth: Tooth -> tooth.toothMorphology == "-4 = Dilaceration" },
            "Natal Teeth" to { tooth: Tooth -> tooth.eruptionStatus == "+2 = Natal teeth" },
            "Neonatal Teeth" to { tooth: Tooth -> tooth.eruptionStatus == "+1 = Neonatal teeth" },
            "Submerged" to { tooth: Tooth -> tooth.eruptionStatus == "-1 = Submerged (1-2mm below)" },
            "Retained" to { tooth: Tooth -> tooth.eruptionStatus == "-2 = Retained (2-4mm below)" },
            "Embedded" to { tooth: Tooth -> tooth.eruptionStatus == "-3 = Embedded (>4mm, soft tissue)" },
            "Impacted" to { tooth: Tooth -> tooth.eruptionStatus == "-4 = Impacted (>4mm, bone obstruction)" },
            "Ectopic Eruption" to { tooth: Tooth -> tooth.eruptionStatus == "-5 = Ectopic eruption" }
        )

        var devAnomaliesFound = false
        developmentalAnomalies.forEach { (label, predicate) ->
            val teeth = state.teeth.values.filter(predicate).map { it.number }
            if (teeth.isNotEmpty()) {
                AnomalySummaryItem(label, teeth.joinToString())
                devAnomaliesFound = true
            }
        }
                if (!devAnomaliesFound) {
                    Text(
                        "None detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // --- Pathological & Traumatic Conditions ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Pathological & Traumatic Conditions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

        val pathologicalAnomalies = listOf(
            "ICDAS 1" to { it: Tooth -> it.caries == "+1 = ICDAS 1 - First visual change" },
            "ICDAS 2" to { it: Tooth -> it.caries == "+2 = ICDAS 2 - Distinct visual change" },
            "ICDAS 3" to { it: Tooth -> it.caries == "+3 = ICDAS 3 - Localized enamel breakdown" },
            "ICDAS 4" to { it: Tooth -> it.caries == "+4 = ICDAS 4 - Dark shadow from dentin" },
            "ICDAS 5" to { it: Tooth -> it.caries == "+5 = ICDAS 5 - Cavity with visible dentin (<50%)" },
            "ICDAS 6" to { it: Tooth -> it.caries == "+6 = ICDAS 6 - Extensive cavity (>50%)" },
            "Root Caries - Active" to { it: Tooth -> it.caries == "+7 = Root caries - Active" },
            "Root Caries - Cavitated" to { it: Tooth -> it.caries == "+8 = Root caries - Cavitated" },
            "Erosion - Initial" to { it: Tooth -> it.erosion == "+1 = Initial loss of surface texture" },
            "Erosion - Defect <50%" to { it: Tooth -> it.erosion == "+2 = Defect with hard tissue loss <50%" },
            "Erosion - Defect ≥50%" to { it: Tooth -> it.erosion == "+3 = Hard tissue loss ≥50%" },
            "Ellis Class I" to { it: Tooth -> it.fractureType == "+1 = Ellis Class I (enamel only)" },
            "Ellis Class II" to { it: Tooth -> it.fractureType == "+2 = Ellis Class II (enamel + dentin)" },
            "Ellis Class III" to { it: Tooth -> it.fractureType == "+3 = Ellis Class III (+ pulp)" },
            "Crown-Root Fracture" to { it: Tooth -> it.fractureType == "+4 = Crown-root fracture" },
            "Horizontal Root Fracture" to { it: Tooth -> it.fractureType == "+5 = Horizontal root fracture" },
            "Vertical Root Fracture" to { it: Tooth -> it.fractureType == "+6 = Vertical root fracture" },
            "Oblique Root Fracture" to { it: Tooth -> it.fractureType == "+7 = Oblique root fracture" },
            "Comminuted Fracture" to { it: Tooth -> it.fractureType == "+8 = Comminuted fracture" },
            "Attrition - Minimal" to { it: Tooth -> it.attrition == "+1 = Minimal (enamel facets)" },
            "Attrition - Moderate" to { it: Tooth -> it.attrition == "+2 = Moderate (dentin exposure <1/3)" },
            "Attrition - Severe" to { it: Tooth -> it.attrition == "+3 = Severe (dentin 1/3-2/3)" },
            "Attrition - Extreme" to { it: Tooth -> it.attrition == "+4 = Extreme (dentin >2/3)" },
            "Abrasion - Minimal" to { it: Tooth -> it.abrasion == "+1 = Minimal (V-notch at CEJ, <1mm)" },
            "Abrasion - Moderate" to { it: Tooth -> it.abrasion == "+2 = Moderate (1-2mm, mild sensitivity)" },
            "Abrasion - Severe" to { it: Tooth -> it.abrasion == "+3 = Severe (2-3mm, moderate sensitivity)" },
            "Abrasion - Extreme" to { it: Tooth -> it.abrasion == "+4 = Extreme (>3mm, severe sensitivity)" },
            "Abfraction - Minimal" to { it: Tooth -> it.abfraction == "+1 = Minimal (wedge defect <1mm)" },
            "Abfraction - Moderate" to { it: Tooth -> it.abfraction == "+2 = Moderate (1-2mm, mild sensitivity)" },
            "Abfraction - Severe" to { it: Tooth -> it.abfraction == "+3 = Severe (2-3mm, moderate sensitivity)" },
            "Abfraction - Extreme" to { it: Tooth -> it.abfraction == "+4 = Extreme (>3mm, severe sensitivity)" },
            "Discoloration - White Spot" to { it: Tooth -> it.discoloration == "+1 = White spot" },
            "Discoloration - Yellow" to { it: Tooth -> it.discoloration == "+2 = Yellow (extrinsic stain)" },
            "Discoloration - Brown" to { it: Tooth -> it.discoloration == "+3 = Brown (fluorosis, staining)" },
            "Discoloration - Gray" to { it: Tooth -> it.discoloration == "+4 = Gray (non-vital, amalgam)" },
            "Discoloration - Black" to { it: Tooth -> it.discoloration == "+5 = Black (severe caries, necrosis)" },
            "Discoloration - Pink" to { it: Tooth -> it.discoloration == "+6 = Pink (internal resorption)" },
            "Discoloration - Tetracycline" to { it: Tooth -> it.discoloration == "+7 = Tetracycline staining" },
            "Discoloration - Amelogenesis Imperfecta" to { it: Tooth -> it.discoloration == "+8 = Amelogenesis imperfecta" }
        )

        var pathAnomaliesFound = false
        pathologicalAnomalies.forEach { (label, predicate) ->
            val teeth = state.teeth.values.filter(predicate).map { it.number }
            if (teeth.isNotEmpty()) {
                AnomalySummaryItem(label, teeth.joinToString())
                pathAnomaliesFound = true
            }
        }
                if (!pathAnomaliesFound) {
                    Text(
                        "None detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // --- Pulp Vitality & Pain Conditions ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Pulp Vitality & Pain Conditions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

        val pulpConditions = listOf(
            "Reversible Pulpitis" to { it: Tooth -> it.painStatus == "+1 = Pain on cold/hot (reversible pulpitis)" },
            "Pain on Stimuli Only" to { it: Tooth -> it.painStatus == "+2 = Pain on stimuli only" },
            "Irreversible Pulpitis" to { it: Tooth -> it.painStatus == "+3 = Prolonged pain without stimuli (irreversible)" },
            "Severe Spontaneous Pain" to { it: Tooth -> it.painStatus == "+4 = Severe spontaneous throbbing pain" },
            "Necrotic Pulp" to { it: Tooth -> it.painStatus == "+5 = No response to stimuli (necrotic pulp)" },
            "Tender on Percussion" to { it: Tooth -> it.tenderOnPercussion == "+1 = Positive (periapical inflammation)" },
            "Vestibular Tenderness" to { it: Tooth -> it.vestibularTenderness == "+1 = Positive (acute periapical abscess)" },
            "Sinus Tract - Opened" to { it: Tooth -> it.sinusTract == "+1 = Opened (draining sinus tract)" },
            "Sinus Tract - Multiple" to { it: Tooth -> it.sinusTract == "+2 = Multiple sinus tracts" }
        )

        var pulpConditionsFound = false
        pulpConditions.forEach { (label, predicate) ->
            val teeth = state.teeth.values.filter(predicate).map { it.number }
            if (teeth.isNotEmpty()) {
                AnomalySummaryItem(label, teeth.joinToString())
                pulpConditionsFound = true
            }
        }
                if (!pulpConditionsFound) {
                    Text(
                        "None detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // --- Periodontal Parameters Summary ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Periodontal Parameters Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                PeriodontalParametersSummary(state)
            }
        }
        
        // --- Malocclusion Summary ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Malocclusion Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (state.patientInfo.malocclusion.isNotEmpty() && state.patientInfo.malocclusion != "None") {
                    SummaryItem("Malocclusion", state.patientInfo.malocclusion)
                } else {
                    Text(
                        "No malocclusion detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        // --- Oral Mucosa Summary ---
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Oral Mucosa Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (state.patientInfo.oralMucosa.isNotEmpty() && state.patientInfo.oralMucosa != "No issues") {
                    SummaryItem("Oral Mucosa Status", state.patientInfo.oralMucosa)
                    // Show lesion characteristics if there are any issues
                    if (state.patientInfo.lesionType.isNotEmpty() && 
                        !state.patientInfo.lesionType.contains("Normal mucosa")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Lesion Characteristics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryItem("Lesion Type", state.patientInfo.lesionType)
                        if (state.patientInfo.lesionSize.isNotEmpty() && 
                            !state.patientInfo.lesionSize.contains("0 =")) {
                            SummaryItem("Lesion Size", state.patientInfo.lesionSize)
                        }
                        if (state.patientInfo.lesionColor.isNotEmpty() && 
                            !state.patientInfo.lesionColor.contains("Normal mucosa color")) {
                            SummaryItem("Lesion Color", state.patientInfo.lesionColor)
                        }
                        if (state.patientInfo.surfaceTexture.isNotEmpty() && 
                            !state.patientInfo.surfaceTexture.contains("Normal texture")) {
                            SummaryItem("Surface Texture", state.patientInfo.surfaceTexture)
                        }
                        if (state.patientInfo.borderDefinition.isNotEmpty() && 
                            !state.patientInfo.borderDefinition.contains("Moderately defined")) {
                            SummaryItem("Border Definition", state.patientInfo.borderDefinition)
                        }
                        if (state.patientInfo.lesionPain.isNotEmpty() && 
                            !state.patientInfo.lesionPain.contains("No pain")) {
                            SummaryItem("Pain", state.patientInfo.lesionPain)
                        }
                        if (state.patientInfo.lesionDuration.isNotEmpty() && 
                            !state.patientInfo.lesionDuration.contains("0 =")) {
                            SummaryItem("Duration", state.patientInfo.lesionDuration)
                        }
                    }
                } else {
                    Text(
                        "No oral mucosa issues detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        // --- Periodontal Assessment Matrix Table ---
        Spacer(modifier = Modifier.height(16.dp))
        Text("Periodontal Assessment", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        PeriodontalMatrixTable(state.gingivalSegments)
        
        // --- Provisional Diagnoses ---
        Spacer(modifier = Modifier.height(24.dp))
        Text("Provisional Diagnoses", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        ProvisionalToothDiagnosisSection(state)
        
        Spacer(modifier = Modifier.height(16.dp))
        ProvisionalPeriodontalDiagnosisSection(state)
        
        Spacer(modifier = Modifier.height(16.dp))
        ProvisionalOralMucosaDiagnosisSection(state)
        
        Spacer(modifier = Modifier.height(16.dp))
        ProvisionalMalocclusionDiagnosisSection(state)
        
        // --- Final Diagnosis ---
        Spacer(modifier = Modifier.height(24.dp))
        Text("Final Diagnosis", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        var finalDiagnosis by remember { mutableStateOf(state.patientInfo.finalDiagnosis) }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = finalDiagnosis,
                    onValueChange = { 
                        finalDiagnosis = it
                        // Save to state immediately
                        stateHolder?.updateFinalDiagnosis(it)
                    },
                    label = { Text("Enter Final Diagnosis") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
        
        // --- Treatment Plan ---
        Spacer(modifier = Modifier.height(24.dp))
        Text("Treatment Plan", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        TreatmentPlanSection(state, stateHolder)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// CDT Code data class
data class CdtCode(
    val code: String,
    val name: String
)

// Treatment Plan Item UI data class (for local UI state)
data class UITreatmentPlanItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cdtCode: CdtCode? = null,
    val description: String = "",
    val timestamp: String = ""
)

// CDT Codes list
val CDT_CODES = listOf(
    CdtCode("D0120", "Periodic oral evaluation"),
    CdtCode("D0140", "Limited oral evaluation"),
    CdtCode("D0150", "Comprehensive oral evaluation"),
    CdtCode("D0160", "Detailed and extensive oral evaluation"),
    CdtCode("D0210", "Intraoral - complete series of radiographic images"),
    CdtCode("D0220", "Intraoral - periapical first radiographic image"),
    CdtCode("D0230", "Intraoral - periapical each additional radiographic image"),
    CdtCode("D0240", "Intraoral - occlusal radiographic image"),
    CdtCode("D0270", "Bitewing - single radiographic image"),
    CdtCode("D0272", "Bitewings - two radiographic images"),
    CdtCode("D0273", "Bitewings - three radiographic images"),
    CdtCode("D0274", "Bitewings - four radiographic images"),
    CdtCode("D0277", "Vertical bitewings - 7 to 8 radiographic images"),
    CdtCode("D1110", "Adult prophylaxis"),
    CdtCode("D1120", "Child prophylaxis"),
    CdtCode("D1206", "Topical fluoride varnish"),
    CdtCode("D1208", "Topical application of fluoride"),
    CdtCode("D1310", "Nutritional counseling for the control of dental disease"),
    CdtCode("D1320", "Tobacco counseling for the control and prevention of oral disease"),
    CdtCode("D1330", "Oral hygiene instructions"),
    CdtCode("D1351", "Sealant - per tooth"),
    CdtCode("D1352", "Preventive resin restoration"),
    CdtCode("D2140", "Amalgam restoration - one surface, primary"),
    CdtCode("D2150", "Amalgam restoration - two surfaces, primary"),
    CdtCode("D2160", "Amalgam restoration - three surfaces, primary"),
    CdtCode("D2161", "Amalgam restoration - four or more surfaces, primary"),
    CdtCode("D2330", "Resin-based composite - one surface, anterior"),
    CdtCode("D2331", "Resin-based composite - two surfaces, anterior"),
    CdtCode("D2332", "Resin-based composite - three surfaces, anterior"),
    CdtCode("D2335", "Resin-based composite - four or more surfaces, anterior"),
    CdtCode("D2391", "Resin-based composite - one surface, posterior"),
    CdtCode("D2392", "Resin-based composite - two surfaces, posterior"),
    CdtCode("D2393", "Resin-based composite - three surfaces, posterior"),
    CdtCode("D2394", "Resin-based composite - four or more surfaces, posterior"),
    CdtCode("D2510", "Inlay - metallic - one surface"),
    CdtCode("D2520", "Inlay - metallic - two surfaces"),
    CdtCode("D2530", "Inlay - metallic - three or more surfaces"),
    CdtCode("D2542", "Onlay - metallic - two surfaces"),
    CdtCode("D2543", "Onlay - metallic - three surfaces"),
    CdtCode("D2544", "Onlay - metallic - four or more surfaces"),
    CdtCode("D2610", "Inlay - porcelain/ceramic - one surface"),
    CdtCode("D2620", "Inlay - porcelain/ceramic - two surfaces"),
    CdtCode("D2630", "Inlay - porcelain/ceramic - three or more surfaces"),
    CdtCode("D2642", "Onlay - porcelain/ceramic - two surfaces"),
    CdtCode("D2643", "Onlay - porcelain/ceramic - three surfaces"),
    CdtCode("D2644", "Onlay - porcelain/ceramic - four or more surfaces"),
    CdtCode("D2710", "Crown - resin-based composite"),
    CdtCode("D2720", "Crown - resin with high noble metal"),
    CdtCode("D2721", "Crown - resin with predominantly base metal"),
    CdtCode("D2722", "Crown - resin with noble metal"),
    CdtCode("D2740", "Crown - porcelain/ceramic substrate"),
    CdtCode("D2750", "Crown - porcelain fused to high noble metal"),
    CdtCode("D2751", "Crown - porcelain fused to predominantly base metal"),
    CdtCode("D2752", "Crown - porcelain fused to noble metal"),
    CdtCode("D2780", "Crown - 3/4 cast high noble metal"),
    CdtCode("D2781", "Crown - 3/4 cast predominantly base metal"),
    CdtCode("D2782", "Crown - 3/4 cast noble metal"),
    CdtCode("D2790", "Crown - full cast high noble metal"),
    CdtCode("D2791", "Crown - full cast predominantly base metal"),
    CdtCode("D2792", "Crown - full cast noble metal"),
    CdtCode("D2910", "Recement inlay, onlay or partial coverage restoration"),
    CdtCode("D2920", "Recement crown"),
    CdtCode("D2930", "Prefabricated stainless steel crown - primary tooth"),
    CdtCode("D2931", "Prefabricated stainless steel crown - permanent tooth"),
    CdtCode("D2932", "Prefabricated resin crown"),
    CdtCode("D2933", "Prefabricated esthetic coated stainless steel crown - primary tooth"),
    CdtCode("D2934", "Prefabricated esthetic coated stainless steel crown - permanent tooth"),
    CdtCode("D2940", "Sedative filling"),
    CdtCode("D2950", "Core buildup, including any pins"),
    CdtCode("D2951", "Pin retention - per tooth"),
    CdtCode("D2952", "Post and core in addition to crown"),
    CdtCode("D2953", "Each additional indirectly fabricated post"),
    CdtCode("D2954", "Prefabricated post and core in addition to crown"),
    CdtCode("D2955", "Post removal"),
    CdtCode("D2957", "Each additional prefabricated post"),
    CdtCode("D2960", "Labial veneer (resin laminate) - chairside"),
    CdtCode("D2961", "Labial veneer (resin laminate) - laboratory"),
    CdtCode("D2962", "Labial veneer (porcelain laminate) - laboratory"),
    CdtCode("D2970", "Crown repair, by report"),
    CdtCode("D2971", "Additional procedures to construct new crown"),
    CdtCode("D2975", "Coping"),
    CdtCode("D2980", "Crown repair necessitated by restorative material failure"),
    CdtCode("D2999", "Unspecified restorative procedure, by report"),
    CdtCode("D3110", "Pulp cap - direct (excluding final restoration)"),
    CdtCode("D3120", "Pulp cap - indirect (excluding final restoration)"),
    CdtCode("D3220", "Therapeutic pulpotomy (excluding final restoration)"),
    CdtCode("D3221", "Pulpal debridement, primary and permanent teeth"),
    CdtCode("D3230", "Pulpal therapy (resorbable filling) - anterior, primary tooth"),
    CdtCode("D3240", "Pulpal therapy (resorbable filling) - posterior, primary tooth"),
    CdtCode("D3310", "Endodontic therapy, anterior tooth (excluding final restoration)"),
    CdtCode("D3320", "Endodontic therapy, bicuspid tooth (excluding final restoration)"),
    CdtCode("D3330", "Endodontic therapy, molar (excluding final restoration)"),
    CdtCode("D3331", "Treatment of root canal obstruction; non-surgical access"),
    CdtCode("D3332", "Incomplete endodontic therapy; inoperable, unrestorable or fractured tooth"),
    CdtCode("D3333", "Internal root repair of perforation defects"),
    CdtCode("D3346", "Retreatment of previous root canal therapy - anterior"),
    CdtCode("D3347", "Retreatment of previous root canal therapy - bicuspid"),
    CdtCode("D3348", "Retreatment of previous root canal therapy - molar"),
    CdtCode("D3351", "Apexification - initial visit"),
    CdtCode("D3352", "Apexification - interim medication replacement"),
    CdtCode("D3353", "Apexification - final visit (including completed root canal therapy)"),
    CdtCode("D3355", "Pulpal regeneration - initial visit"),
    CdtCode("D3356", "Pulpal regeneration - interim medication replacement"),
    CdtCode("D3357", "Pulpal regeneration - final visit (including completed root canal therapy)"),
    CdtCode("D3410", "Apicoectomy/periradicular surgery - anterior"),
    CdtCode("D3421", "Apicoectomy/periradicular surgery - bicuspid (first root)"),
    CdtCode("D3425", "Apicoectomy/periradicular surgery - molar (first root)"),
    CdtCode("D3426", "Apicoectomy/periradicular surgery (each additional root)"),
    CdtCode("D3430", "Retrograde filling - per root"),
    CdtCode("D3450", "Root amputation - per root"),
    CdtCode("D3460", "Endodontic endosseous implant"),
    CdtCode("D3470", "Intentional reimplantation (including necessary splinting)"),
    CdtCode("D3910", "Surgical procedure for isolation of tooth with rubber dam"),
    CdtCode("D3920", "Hemorrhage control"),
    CdtCode("D3940", "Periodontal scaling and root planing - per quadrant"),
    CdtCode("D4341", "Periodontal scaling and root planing - four or more teeth per quadrant"),
    CdtCode("D4342", "Periodontal scaling and root planing - one to three teeth per quadrant"),
    CdtCode("D4355", "Full mouth debridement to enable comprehensive evaluation and diagnosis"),
    CdtCode("D4381", "Localized delivery of antimicrobial agents via a controlled release vehicle"),
    CdtCode("D4910", "Periodontal maintenance"),
    CdtCode("D4920", "Unspecified periodontal procedure, by report"),
    CdtCode("D6010", "Surgical placement of implant body: endosteal implant"),
    CdtCode("D6011", "Second stage implant surgery"),
    CdtCode("D6012", "Surgical placement of interim implant body for transitional prosthesis"),
    CdtCode("D6013", "Surgical placement of mini implant"),
    CdtCode("D6040", "Implant supported crown"),
    CdtCode("D6050", "Implant supported crown - porcelain/ceramic"),
    CdtCode("D6051", "Implant supported crown - porcelain fused to metal"),
    CdtCode("D6052", "Implant supported crown - full cast metal"),
    CdtCode("D6053", "Implant supported crown - resin-based composite"),
    CdtCode("D6054", "Implant supported crown - titanium"),
    CdtCode("D6055", "Implant supported crown - zirconia"),
    CdtCode("D6056", "Implant supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6057", "Implant supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6058", "Implant supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6059", "Implant supported crown - titanium with titanium"),
    CdtCode("D6060", "Implant supported crown - zirconia with zirconia"),
    CdtCode("D6061", "Implant supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6062", "Implant supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6063", "Implant supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6064", "Implant supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6065", "Implant supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6066", "Implant supported crown - titanium with titanium"),
    CdtCode("D6067", "Implant supported crown - zirconia with zirconia"),
    CdtCode("D6068", "Implant supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6069", "Implant supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6070", "Implant supported abutment - porcelain/ceramic"),
    CdtCode("D6071", "Implant supported abutment - porcelain fused to metal"),
    CdtCode("D6072", "Implant supported abutment - full cast metal"),
    CdtCode("D6073", "Implant supported abutment - resin-based composite"),
    CdtCode("D6074", "Implant supported abutment - titanium"),
    CdtCode("D6075", "Implant supported abutment - zirconia"),
    CdtCode("D6076", "Implant supported abutment - porcelain/ceramic with high noble metal"),
    CdtCode("D6077", "Implant supported abutment - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6078", "Implant supported abutment - porcelain/ceramic with noble metal"),
    CdtCode("D6079", "Implant supported abutment - titanium with titanium"),
    CdtCode("D6080", "Implant supported abutment - zirconia with zirconia"),
    CdtCode("D6081", "Implant supported abutment - porcelain/ceramic with titanium"),
    CdtCode("D6082", "Implant supported abutment - porcelain/ceramic with zirconia"),
    CdtCode("D6090", "Repair implant supported crown"),
    CdtCode("D6091", "Repair implant supported abutment"),
    CdtCode("D6092", "Recement implant supported crown"),
    CdtCode("D6093", "Recement implant supported abutment"),
    CdtCode("D6094", "Repair implant supported crown - porcelain/ceramic"),
    CdtCode("D6095", "Repair implant supported crown - porcelain fused to metal"),
    CdtCode("D6100", "Implant removal"),
    CdtCode("D6190", "Radiographic/surgical implant index"),
    CdtCode("D6194", "Abutment supported crown - porcelain/ceramic"),
    CdtCode("D6195", "Abutment supported crown - porcelain fused to metal"),
    CdtCode("D6196", "Abutment supported crown - full cast metal"),
    CdtCode("D6197", "Abutment supported crown - resin-based composite"),
    CdtCode("D6198", "Abutment supported crown - titanium"),
    CdtCode("D6199", "Abutment supported crown - zirconia"),
    CdtCode("D6200", "Abutment supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6201", "Abutment supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6202", "Abutment supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6203", "Abutment supported crown - titanium with titanium"),
    CdtCode("D6204", "Abutment supported crown - zirconia with zirconia"),
    CdtCode("D6205", "Abutment supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6206", "Abutment supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6210", "Abutment supported crown - porcelain/ceramic"),
    CdtCode("D6211", "Abutment supported crown - porcelain fused to metal"),
    CdtCode("D6212", "Abutment supported crown - full cast metal"),
    CdtCode("D6213", "Abutment supported crown - resin-based composite"),
    CdtCode("D6214", "Abutment supported crown - titanium"),
    CdtCode("D6215", "Abutment supported crown - zirconia"),
    CdtCode("D6216", "Abutment supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6217", "Abutment supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6218", "Abutment supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6219", "Abutment supported crown - titanium with titanium"),
    CdtCode("D6220", "Abutment supported crown - zirconia with zirconia"),
    CdtCode("D6221", "Abutment supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6222", "Abutment supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6230", "Abutment supported crown - porcelain/ceramic"),
    CdtCode("D6231", "Abutment supported crown - porcelain fused to metal"),
    CdtCode("D6232", "Abutment supported crown - full cast metal"),
    CdtCode("D6233", "Abutment supported crown - resin-based composite"),
    CdtCode("D6234", "Abutment supported crown - titanium"),
    CdtCode("D6235", "Abutment supported crown - zirconia"),
    CdtCode("D6236", "Abutment supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6237", "Abutment supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6238", "Abutment supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6239", "Abutment supported crown - titanium with titanium"),
    CdtCode("D6240", "Abutment supported crown - zirconia with zirconia"),
    CdtCode("D6241", "Abutment supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6242", "Abutment supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6250", "Abutment supported crown - porcelain/ceramic"),
    CdtCode("D6251", "Abutment supported crown - porcelain fused to metal"),
    CdtCode("D6252", "Abutment supported crown - full cast metal"),
    CdtCode("D6253", "Abutment supported crown - resin-based composite"),
    CdtCode("D6254", "Abutment supported crown - titanium"),
    CdtCode("D6255", "Abutment supported crown - zirconia"),
    CdtCode("D6256", "Abutment supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6257", "Abutment supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6258", "Abutment supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6259", "Abutment supported crown - titanium with titanium"),
    CdtCode("D6260", "Abutment supported crown - zirconia with zirconia"),
    CdtCode("D6261", "Abutment supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6262", "Abutment supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D6270", "Abutment supported crown - porcelain/ceramic"),
    CdtCode("D6271", "Abutment supported crown - porcelain fused to metal"),
    CdtCode("D6272", "Abutment supported crown - full cast metal"),
    CdtCode("D6273", "Abutment supported crown - resin-based composite"),
    CdtCode("D6274", "Abutment supported crown - titanium"),
    CdtCode("D6275", "Abutment supported crown - zirconia"),
    CdtCode("D6276", "Abutment supported crown - porcelain/ceramic with high noble metal"),
    CdtCode("D6277", "Abutment supported crown - porcelain/ceramic with predominantly base metal"),
    CdtCode("D6278", "Abutment supported crown - porcelain/ceramic with noble metal"),
    CdtCode("D6279", "Abutment supported crown - titanium with titanium"),
    CdtCode("D6280", "Abutment supported crown - zirconia with zirconia"),
    CdtCode("D6281", "Abutment supported crown - porcelain/ceramic with titanium"),
    CdtCode("D6282", "Abutment supported crown - porcelain/ceramic with zirconia"),
    CdtCode("D7111", "Extraction, coronal remnants - deciduous tooth"),
    CdtCode("D7120", "Extraction, erupted tooth or exposed root"),
    CdtCode("D7140", "Extraction, erupted tooth requiring removal of bone and/or sectioning of tooth"),
    CdtCode("D7210", "Extraction, erupted tooth requiring removal of bone and/or sectioning of tooth"),
    CdtCode("D7220", "Removal of impacted tooth - soft tissue"),
    CdtCode("D7230", "Removal of impacted tooth - partially bony"),
    CdtCode("D7240", "Removal of impacted tooth - completely bony"),
    CdtCode("D7241", "Removal of impacted tooth - completely bony, with unusual surgical complications"),
    CdtCode("D7250", "Removal of residual tooth roots (cutting procedure)"),
    CdtCode("D7260", "Oroantral fistula closure"),
    CdtCode("D7261", "Primary closure of a sinus perforation"),
    CdtCode("D7270", "Tooth reimplantation and/or stabilization of accidentally evulsed or displaced tooth"),
    CdtCode("D7272", "Tooth transplantation (includes reimplantation from one site to another)"),
    CdtCode("D7280", "Exposure of an unerupted tooth"),
    CdtCode("D7282", "Mobilization of erupted or malpositioned tooth to aid eruption"),
    CdtCode("D7283", "Placement of device to facilitate eruption of impacted tooth"),
    CdtCode("D7290", "Transalveolar transplantation of tooth"),
    CdtCode("D7291", "Surgical repositioning of teeth"),
    CdtCode("D7292", "Surgical repositioning of teeth with rigid internal fixation"),
    CdtCode("D7293", "Surgical repositioning of teeth with splinting"),
    CdtCode("D7294", "Surgical repositioning of teeth with orthodontic appliances"),
    CdtCode("D7295", "Surgical repositioning of teeth with removable appliances"),
    CdtCode("D7310", "Alveoloplasty in conjunction with extractions - one to three teeth"),
    CdtCode("D7311", "Alveoloplasty in conjunction with extractions - four or more teeth"),
    CdtCode("D7320", "Alveoloplasty not in conjunction with extractions - one to three teeth"),
    CdtCode("D7321", "Alveoloplasty not in conjunction with extractions - four or more teeth"),
    CdtCode("D7340", "Vestibuloplasty"),
    CdtCode("D7350", "Vestibuloplasty - ridge extension (secondary epithelialization)"),
    CdtCode("D7410", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7411", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7412", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7413", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7414", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7415", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7420", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7421", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7430", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7431", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7440", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7441", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7450", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7451", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7460", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7461", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7465", "Biopsy of oral tissue - hard (bone, tooth)"),
    CdtCode("D7471", "Removal of lateral exostosis (maxilla or mandible)"),
    CdtCode("D7472", "Removal of torus palatinus"),
    CdtCode("D7473", "Removal of torus mandibularis"),
    CdtCode("D7485", "Surgical reduction of osseous tuberosity"),
    CdtCode("D7490", "Radical alveolectomy"),
    CdtCode("D7510", "Incision and drainage of abscess - intraoral soft tissue"),
    CdtCode("D7511", "Incision and drainage of abscess - intraoral soft tissue - complicated"),
    CdtCode("D7520", "Incision and drainage of abscess - extraoral soft tissue"),
    CdtCode("D7521", "Incision and drainage of abscess - extraoral soft tissue - complicated"),
    CdtCode("D7530", "Removal of foreign body from mucosa, skin, or alveolar bone"),
    CdtCode("D7540", "Removal of reaction-producing foreign bodies, musculoskeletal system"),
    CdtCode("D7550", "Partial ostectomy/sequestrectomy for removal of non-vital bone"),
    CdtCode("D7560", "Maxillary sinusotomy for removal of tooth fragment, foreign body, or antral polyps"),
    CdtCode("D7610", "Extraoral incision and drainage of abscess - soft tissue"),
    CdtCode("D7611", "Extraoral incision and drainage of abscess - soft tissue - complicated"),
    CdtCode("D7620", "Extraoral incision and drainage of abscess - deep fascial space"),
    CdtCode("D7621", "Extraoral incision and drainage of abscess - deep fascial space - complicated"),
    CdtCode("D7630", "Extraoral removal of foreign body"),
    CdtCode("D7640", "Intraoral incision and drainage of abscess - soft tissue"),
    CdtCode("D7641", "Intraoral incision and drainage of abscess - soft tissue - complicated"),
    CdtCode("D7650", "Intraoral incision and drainage of abscess - deep fascial space"),
    CdtCode("D7651", "Intraoral incision and drainage of abscess - deep fascial space - complicated"),
    CdtCode("D7660", "Intraoral removal of foreign body"),
    CdtCode("D7670", "Alveoloplasty - surgical preparation of ridge for dentures"),
    CdtCode("D7671", "Alveoloplasty - surgical preparation of ridge for dentures - four or more teeth"),
    CdtCode("D7680", "Frenulectomy (frenumectomy, frenulectomy) - separate procedure"),
    CdtCode("D7681", "Frenuloplasty - surgical revision of frenum"),
    CdtCode("D7710", "Extraction, coronal remnants - deciduous tooth"),
    CdtCode("D7720", "Extraction, erupted tooth or exposed root"),
    CdtCode("D7730", "Extraction, erupted tooth requiring removal of bone and/or sectioning of tooth"),
    CdtCode("D7740", "Extraction, erupted tooth requiring removal of bone and/or sectioning of tooth"),
    CdtCode("D7750", "Removal of impacted tooth - soft tissue"),
    CdtCode("D7760", "Removal of impacted tooth - partially bony"),
    CdtCode("D7770", "Removal of impacted tooth - completely bony"),
    CdtCode("D7780", "Removal of impacted tooth - completely bony, with unusual surgical complications"),
    CdtCode("D7790", "Removal of residual tooth roots (cutting procedure)"),
    CdtCode("D7810", "Oroantral fistula closure"),
    CdtCode("D7811", "Primary closure of a sinus perforation"),
    CdtCode("D7820", "Tooth reimplantation and/or stabilization of accidentally evulsed or displaced tooth"),
    CdtCode("D7830", "Tooth transplantation (includes reimplantation from one site to another)"),
    CdtCode("D7840", "Exposure of an unerupted tooth"),
    CdtCode("D7850", "Surgical repositioning of teeth"),
    CdtCode("D7852", "Surgical repositioning of teeth with rigid internal fixation"),
    CdtCode("D7854", "Surgical repositioning of teeth with splinting"),
    CdtCode("D7856", "Surgical repositioning of teeth with orthodontic appliances"),
    CdtCode("D7858", "Surgical repositioning of teeth with removable appliances"),
    CdtCode("D7860", "Alveoloplasty in conjunction with extractions - one to three teeth"),
    CdtCode("D7861", "Alveoloplasty in conjunction with extractions - four or more teeth"),
    CdtCode("D7862", "Alveoloplasty not in conjunction with extractions - one to three teeth"),
    CdtCode("D7863", "Alveoloplasty not in conjunction with extractions - four or more teeth"),
    CdtCode("D7864", "Vestibuloplasty"),
    CdtCode("D7865", "Vestibuloplasty - ridge extension (secondary epithelialization)"),
    CdtCode("D7870", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7871", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7872", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7873", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7874", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7875", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7876", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7877", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7878", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7879", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7880", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7881", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7882", "Removal of benign odontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7883", "Removal of benign odontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7884", "Removal of benign nonodontogenic cyst or tumor - lesion diameter up to 1.25 cm"),
    CdtCode("D7885", "Removal of benign nonodontogenic cyst or tumor - lesion diameter greater than 1.25 cm"),
    CdtCode("D7886", "Biopsy of oral tissue - hard (bone, tooth)"),
    CdtCode("D7890", "Unspecified oral surgery procedure, by report"),
    CdtCode("D7910", "Suture of recent small wounds up to 5 cm"),
    CdtCode("D7911", "Complicated suture - up to 5 cm"),
    CdtCode("D7912", "Complicated suture - greater than 5 cm"),
    CdtCode("D7920", "Skin graft (identify defect covered, location, and type of graft)"),
    CdtCode("D7940", "Osteoplasty - cosmetic"),
    CdtCode("D7941", "Osteotomy - cosmetic"),
    CdtCode("D7943", "Osteoplasty - functional"),
    CdtCode("D7944", "Osteotomy - functional"),
    CdtCode("D7945", "Osteoplasty - reconstructive"),
    CdtCode("D7946", "Osteotomy - reconstructive"),
    CdtCode("D7947", "Osteoplasty - surgical"),
    CdtCode("D7948", "Osteotomy - surgical"),
    CdtCode("D7949", "Osteoplasty - unspecified"),
    CdtCode("D7950", "Osteotomy - unspecified"),
    CdtCode("D7951", "Sinus augmentation with bone or bone substitutes"),
    CdtCode("D7952", "Sinus augmentation with bone or bone substitutes - staged"),
    CdtCode("D7953", "Bone replacement graft for ridge preservation - per site"),
    CdtCode("D7955", "Biologic materials to aid in soft and osseous tissue regeneration"),
    CdtCode("D7960", "Frenulectomy (frenumectomy, frenulectomy) - separate procedure"),
    CdtCode("D7961", "Frenuloplasty - surgical revision of frenum"),
    CdtCode("D7970", "Excision of hyperplastic tissue - per arch"),
    CdtCode("D7971", "Excision of pericoronal gingiva"),
    CdtCode("D7972", "Surgical reduction of fibrous tuberosity"),
    CdtCode("D7980", "Sinus augmentation with bone or bone substitutes"),
    CdtCode("D7981", "Sinus augmentation with bone or bone substitutes - staged"),
    CdtCode("D7982", "Bone replacement graft for ridge preservation - per site"),
    CdtCode("D7983", "Biologic materials to aid in soft and osseous tissue regeneration"),
    CdtCode("D7990", "Emergency palliative treatment of dental pain"),
    CdtCode("D7991", "Coronal polishing - adult"),
    CdtCode("D7992", "Coronal polishing - child"),
    CdtCode("D7993", "Air polishing - per visit"),
    CdtCode("D7994", "Selective polishing"),
    CdtCode("D7995", "Full mouth debridement"),
    CdtCode("D9110", "Palliative (emergency) treatment of dental pain - minor procedure"),
    CdtCode("D9120", "Fixed partial denture repair, by report"),
    CdtCode("D9130", "Removable prosthodontic repair, by report"),
    CdtCode("D9210", "Local anesthesia not in conjunction with operative or surgical procedure"),
    CdtCode("D9211", "Regional block anesthesia"),
    CdtCode("D9212", "Trigeminal division block anesthesia"),
    CdtCode("D9215", "Local anesthesia not in conjunction with operative or surgical procedure"),
    CdtCode("D9220", "Deep sedation/general anesthesia - first 30 minutes"),
    CdtCode("D9221", "Deep sedation/general anesthesia - each additional 15 minutes"),
    CdtCode("D9230", "Analgesia, anxiolysis, inhalation of nitrous oxide/oxygen"),
    CdtCode("D9241", "Intravenous conscious sedation/analgesia - first 30 minutes"),
    CdtCode("D9242", "Intravenous conscious sedation/analgesia - each additional 15 minutes"),
    CdtCode("D9243", "Non-intravenous conscious sedation"),
    CdtCode("D9248", "Non-intravenous conscious sedation - each additional 15 minutes"),
    CdtCode("D9310", "Consultation - diagnostic service provided by dentist or physician other than requesting dentist or physician"),
    CdtCode("D9320", "Consultation - diagnostic service provided by dentist or physician other than requesting dentist or physician"),
    CdtCode("D9330", "Visit to office or facility for observation during general anesthesia"),
    CdtCode("D9340", "Visit to office or facility for observation during general anesthesia"),
    CdtCode("D9410", "House/extended care facility call"),
    CdtCode("D9420", "Hospital call"),
    CdtCode("D9430", "Office visit for observation (during regularly scheduled hours) - no other services performed"),
    CdtCode("D9440", "Office visit for observation (during regularly scheduled hours) - no other services performed"),
    CdtCode("D9450", "Case presentation, detailed and extensive treatment planning"),
    CdtCode("D9610", "Therapeutic parenteral drug, single administration"),
    CdtCode("D9612", "Therapeutic parenteral drug, two or more administrations, different medications"),
    CdtCode("D9630", "Other drugs and/or medicaments, by report"),
    CdtCode("D9910", "Application of desensitizing medicament"),
    CdtCode("D9911", "Application of desensitizing resin for cervical hypersensitivity"),
    CdtCode("D9920", "Behavior management, by report"),
    CdtCode("D9930", "Treatment of complications (post-surgical) - unusual circumstances, by report"),
    CdtCode("D9940", "Occlusal adjustment - limited"),
    CdtCode("D9941", "Occlusal adjustment - complete"),
    CdtCode("D9942", "Occlusal adjustment - therapy"),
    CdtCode("D9943", "Occlusal guard - hard appliance, maxillary"),
    CdtCode("D9944", "Occlusal guard - hard appliance, mandibular"),
    CdtCode("D9945", "Occlusal guard - soft appliance, maxillary"),
    CdtCode("D9946", "Occlusal guard - soft appliance, mandibular"),
    CdtCode("D9950", "Occlusal adjustment - limited"),
    CdtCode("D9951", "Occlusal adjustment - complete"),
    CdtCode("D9952", "Occlusal adjustment - therapy"),
    CdtCode("D9960", "Bleaching - in-office, per arch"),
    CdtCode("D9970", "Bleaching - in-office, per arch"),
    CdtCode("D9971", "Bleaching - external bleaching for home application, per arch"),
    CdtCode("D9972", "Bleaching - external bleaching for home application, per arch"),
    CdtCode("D9975", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9980", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9981", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9982", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9985", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9986", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9987", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9990", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9991", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9992", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9993", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9994", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9995", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9996", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9997", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9998", "Bleaching - internal (non-vital) - single tooth"),
    CdtCode("D9999", "Unspecified adjunctive procedure, by report")
)

// Helper function to format timestamp
fun formatTimestamp(timestamp: String): String {
    if (timestamp.isEmpty()) return ""
    return try {
        // Try to parse as long timestamp (milliseconds)
        val timestampLong = timestamp.toLongOrNull()
        if (timestampLong != null && timestampLong > 0) {
            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestampLong))
        } else {
            timestamp // Return as-is if not a valid timestamp
        }
    } catch (e: Exception) {
        timestamp // Return as-is on error
    }
}

// Helper function to get current timestamp string
fun getCurrentTimestampString(): String {
    return System.currentTimeMillis().toString()
}

@Composable
fun TreatmentPlanSection(state: DentalState, stateHolder: DentalStateHolder?) {
    var treatmentPlans by remember { 
        mutableStateOf(
            if (state.patientInfo.treatmentPlans.isEmpty()) 
                listOf(UITreatmentPlanItem(
                    id = java.util.UUID.randomUUID().toString(),
                    timestamp = getCurrentTimestampString()
                )) 
            else 
                state.patientInfo.treatmentPlans.map { 
                    UITreatmentPlanItem(
                        id = it.id.ifEmpty { java.util.UUID.randomUUID().toString() },
                        cdtCode = if (it.cdtCode.isNotEmpty() && it.cdtName.isNotEmpty()) CdtCode(it.cdtCode, it.cdtName) else null,
                        description = it.description,
                        timestamp = it.timestamp.ifEmpty { getCurrentTimestampString() }
                    )
                }
        )
    }
    
    // Save to state whenever plans change
    LaunchedEffect(treatmentPlans) {
        val plansToSave = treatmentPlans.map {
            com.ram.orai.oraic.TreatmentPlanItem(
                id = it.id,
                cdtCode = it.cdtCode?.code ?: "",
                cdtName = it.cdtCode?.name ?: "",
                description = it.description,
                timestamp = it.timestamp.ifEmpty { getCurrentTimestampString() }
            )
        }
        stateHolder?.updateTreatmentPlans(plansToSave)
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Treatment Plans",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            treatmentPlans.forEachIndexed { index, plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Treatment Plan ${index + 1}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (plan.timestamp.isNotEmpty()) {
                                    Text(
                                        formatTimestamp(plan.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (treatmentPlans.size > 1) {
                                TextButton(onClick = {
                                    treatmentPlans = treatmentPlans.filter { it.id != plan.id }
                                }) {
                                    Text(
                                        text = "✕",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // CDT Code Autocomplete
                        var cdtSearchText by remember(plan.id) { mutableStateOf(plan.cdtCode?.let { "${it.code} - ${it.name}" } ?: "") }
                        var selectedCdtCode by remember(plan.id) { mutableStateOf<CdtCode?>(plan.cdtCode) }
                        var showSuggestions by remember { mutableStateOf(false) }
                        
                        // Filter CDT codes based on search text
                        val filteredCdtCodes = remember(cdtSearchText) {
                            if (cdtSearchText.isBlank()) {
                                CDT_CODES.take(10) // Show first 10 when empty
                            } else {
                                CDT_CODES.filter { cdt ->
                                    cdt.code.contains(cdtSearchText, ignoreCase = true) ||
                                    cdt.name.contains(cdtSearchText, ignoreCase = true) ||
                                    "${cdt.code} - ${cdt.name}".contains(cdtSearchText, ignoreCase = true)
                                }.take(10)
                            }
                        }
                        
                        Column {
                            OutlinedTextField(
                                value = cdtSearchText,
                                onValueChange = { newValue ->
                                    cdtSearchText = newValue
                                    showSuggestions = newValue.isNotEmpty()
                                    // Clear selection if text doesn't match
                                    if (selectedCdtCode != null) {
                                        val currentText = selectedCdtCode?.let { "${it.code} - ${it.name}" } ?: ""
                                        if (newValue != currentText) {
                                            selectedCdtCode = null
                                            treatmentPlans = treatmentPlans.map {
                                                if (it.id == plan.id) it.copy(cdtCode = null)
                                                else it
                                            }
                                        }
                                    }
                                },
                                label = { Text("CDT Code (Type to search)") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    if (cdtSearchText.isNotEmpty()) {
                                        IconButton(onClick = {
                                            cdtSearchText = ""
                                            selectedCdtCode = null
                                            showSuggestions = false
                                            treatmentPlans = treatmentPlans.map {
                                                if (it.id == plan.id) it.copy(cdtCode = null)
                                                else it
                                            }
                                        }) {
                                            Text(
                                                text = "✕",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            )
                            
                            // Suggestions Dropdown
                            if (showSuggestions && filteredCdtCodes.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        filteredCdtCodes.forEach { cdtCode ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedCdtCode = cdtCode
                                                        cdtSearchText = "${cdtCode.code} - ${cdtCode.name}"
                                                        showSuggestions = false
                                                        treatmentPlans = treatmentPlans.map {
                                                            if (it.id == plan.id) it.copy(cdtCode = cdtCode)
                                                            else it
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = cdtCode.code,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = cdtCode.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (cdtCode != filteredCdtCodes.last()) {
                                                Divider()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Description field
                        var description by remember { mutableStateOf(plan.description) }
                        OutlinedTextField(
                            value = description,
                            onValueChange = { newDescription ->
                                description = newDescription
                                treatmentPlans = treatmentPlans.map {
                                    if (it.id == plan.id) it.copy(description = newDescription)
                                    else it
                                }
                            },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add Treatment Plan Button
            OutlinedButton(
                onClick = {
                    treatmentPlans = treatmentPlans + UITreatmentPlanItem(
                        timestamp = getCurrentTimestampString()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Treatment Plan")
            }
        }
    }
}

@Composable
fun ToothDiagnosticSection(state: DentalState) {
    val diagnosticEngine = remember { ToothDiagnosticEngine() }
    
    // Get teeth with abnormalities (non-healthy status or non-default values)
    // Exclude "not examined" and "missing" from abnormal list
    val abnormalTeeth = state.teeth.values.filter { tooth ->
        (tooth.status != "normal" && tooth.status != "not examined" && tooth.status != "missing") ||
        tooth.caries != "0 = ICDAS 0 - Sound tooth" ||
        tooth.fractureType != "0 = No fracture" ||
        tooth.erosion != "0 = No erosive wear" ||
        tooth.painStatus != "0 = No pain (asymptomatic)" ||
        tooth.tenderOnPercussion != "0 = Negative" ||
        tooth.vestibularTenderness != "0 = Negative" ||
        tooth.sinusTract != "0 = Normal (no sinus tract)"
    }
    
    if (abnormalTeeth.isEmpty()) {
        Text("No abnormal teeth detected", style = MaterialTheme.typography.bodyLarge)
        return
    }
    
    abnormalTeeth.forEach { tooth ->
        val diagnosis = diagnosticEngine.diagnoseTooth(tooth, state.patientInfo)
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tooth ${tooth.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Primary Diagnosis
                diagnosis.primaryDiagnosis?.let { primary ->
                    Text(
                        text = "Primary Diagnosis: ${primary.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Probability: ${(primary.probability * 100).format(1)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (primary.icd11Primary.isNotEmpty()) {
                        Text(
                            text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) " | ${primary.icd11Secondary}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } ?: run {
                    Text(
                        text = "Primary Diagnosis: Healthy Tooth",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Differential Diagnoses
                if (diagnosis.differentialDiagnoses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Differential Diagnoses:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    diagnosis.differentialDiagnoses.forEach { diff ->
                        Text(
                            text = "• ${diff.name} (${(diff.probability * 100).format(1)}%) - ${diff.icd11Primary}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodontalParametersSummary(state: DentalState) {
    // Group teeth by mobility class
    val mobilityGroups = state.teeth.values
        .filter { it.mobility != "None" }
        .groupBy { it.mobility }
        .mapValues { (_, teeth) -> teeth.map { it.number }.sorted() }
    
    // Group teeth by pocket depth
    val pocketGroups = state.teeth.values
        .filter { it.pocket != "Normal 1-3mm" && !it.pocket.contains("1-3mm") }
        .groupBy { it.pocket }
        .mapValues { (_, teeth) -> teeth.map { it.number }.sorted() }
    
    // Group teeth by recession
    val recessionGroups = state.teeth.values
        .filter { it.recession != "None" }
        .groupBy { it.recession }
        .mapValues { (_, teeth) -> teeth.map { it.number }.sorted() }
    
    // Group teeth by furcation
    val furcationGroups = state.teeth.values
        .filter { it.furcation != "None" }
        .groupBy { it.furcation }
        .mapValues { (_, teeth) -> teeth.map { it.number }.sorted() }
    
    var hasAnyParameter = false
    
    // Display Mobility
    if (mobilityGroups.isNotEmpty()) {
        hasAnyParameter = true
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mobility",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        mobilityGroups.forEach { (mobilityClass, toothNumbers) ->
            AnomalySummaryItem(
                label = mobilityClass,
                teeth = toothNumbers.joinToString(", ")
            )
        }
    }
    
    // Display Pocket Depth
    if (pocketGroups.isNotEmpty()) {
        hasAnyParameter = true
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Pocket Depth",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        pocketGroups.forEach { (pocketDepth, toothNumbers) ->
            AnomalySummaryItem(
                label = pocketDepth,
                teeth = toothNumbers.joinToString(", ")
            )
        }
    }
    
    // Display Recession
    if (recessionGroups.isNotEmpty()) {
        hasAnyParameter = true
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Recession",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        recessionGroups.forEach { (recessionType, toothNumbers) ->
            AnomalySummaryItem(
                label = recessionType,
                teeth = toothNumbers.joinToString(", ")
            )
        }
    }
    
    // Display Furcation
    if (furcationGroups.isNotEmpty()) {
        hasAnyParameter = true
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Furcation Involvement",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        furcationGroups.forEach { (furcationType, toothNumbers) ->
            AnomalySummaryItem(
                label = furcationType,
                teeth = toothNumbers.joinToString(", ")
            )
        }
    }
    
    if (!hasAnyParameter) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No periodontal parameters detected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun OralMucosaDiagnosticSection(state: DentalState) {
    val diagnosticEngine = remember { OralMucosaDiagnosticEngine() }
    
    // Get overall diagnosis
    val diagnosis = diagnosticEngine.diagnose(state.patientInfo)
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            diagnosis.primaryDiagnosis?.let { primary ->
                Text(
                    text = "Primary Diagnosis: ${primary.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (primary.icd11Primary.isNotEmpty() && primary.icd11Primary != "None") {
                    Text(
                        text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) ", ${primary.icd11Secondary}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "Probability: ${(primary.probability * 100).format(1)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (primary.symbolicMatch) {
                    Text(
                        text = "Diagnosis Method: Symbolic Logic (Textbook Rule)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Diagnosis Method: Vector Cosine Similarity + Bayesian Inference",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Cosine Similarity: ${(primary.cosineSimilarity * 100).format(1)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Differential Diagnoses
                if (diagnosis.differentialDiagnoses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Differential Diagnoses:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    diagnosis.differentialDiagnoses.forEach { diff ->
                        Text(
                            text = "• ${diff.name} (${(diff.probability * 100).format(1)}%) - ${diff.icd11Primary}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Primary Diagnosis: Normal Oral Mucosa",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MalocclusionDiagnosticSection(state: DentalState) {
    val diagnosticEngine = remember { MalocclusionDiagnosticEngine() }
    
    // Get overall diagnosis
    val diagnosis = diagnosticEngine.diagnose(state.patientInfo)
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            diagnosis.primaryDiagnosis?.let { primary ->
                Text(
                    text = "Primary Diagnosis: ${primary.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (primary.icd11Primary.isNotEmpty() && primary.icd11Primary != "None") {
                    Text(
                        text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) ", ${primary.icd11Secondary}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Text(
                    text = "Probability: ${(primary.probability * 100).format(1)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                if (primary.symbolicMatch) {
                    Text(
                        text = "Diagnosis Method: Symbolic Logic (Textbook Rule)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Diagnosis Method: Vector Cosine Similarity + Bayesian Inference",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Cosine Similarity: ${(primary.cosineSimilarity * 100).format(1)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Differential Diagnoses
                if (diagnosis.differentialDiagnoses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Differential Diagnoses:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    diagnosis.differentialDiagnoses.forEach { diff ->
                        Text(
                            text = "• ${diff.name} (${(diff.probability * 100).format(1)}%) - ${diff.icd11Primary}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Primary Diagnosis: Normal Occlusion",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PeriodontalDiagnosticSection(state: DentalState) {
    val diagnosticEngine = remember { PeriodontalDiagnosticEngine() }
    
    // Get segments with abnormalities
    val abnormalSegments = state.gingivalSegments.values.filter { segment ->
        segment.bleeding != "No" ||
        segment.plaque != "None" ||
        segment.calculus != "None" ||
        segment.exudate != "None" ||
        segment.color != "Pink" ||
        segment.sizeEdema != "Normal" ||
        segment.texture != "Stippled (normal)" ||
        segment.consistency != "Firm and resilient"
    }
    
    // Overall diagnosis
    val overallDiagnosis = diagnosticEngine.diagnoseOverall(
        state.gingivalSegments,
        state.patientInfo,
        state.teeth
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Overall Periodontal Diagnosis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            overallDiagnosis.primaryDiagnosis?.let { primary ->
                Text(
                    text = "Primary Diagnosis: ${primary.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Probability: ${(primary.probability * 100).format(1)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (primary.icd11Primary.isNotEmpty()) {
                    Text(
                        text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) " | ${primary.icd11Secondary}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } ?: run {
                Text(
                    text = "Primary Diagnosis: Healthy Periodontium",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Differential Diagnoses
            if (overallDiagnosis.differentialDiagnoses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Differential Diagnoses:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                overallDiagnosis.differentialDiagnoses.forEach { diff ->
                    Text(
                        text = "• ${diff.name} (${(diff.probability * 100).format(1)}%) - ${diff.icd11Primary}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
    
    // Segment-specific diagnoses
    if (abnormalSegments.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Segment-Specific Diagnoses",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        abnormalSegments.forEach { segment ->
            val segmentDiagnosis = diagnosticEngine.diagnoseSegment(
                segment,
                state.patientInfo,
                state.teeth
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Segment ${segment.number}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    segmentDiagnosis.primaryDiagnosis?.let { primary ->
                        Text(
                            text = "${primary.name} (${(primary.probability * 100).format(1)}%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (primary.icd11Primary.isNotEmpty()) {
                            Text(
                                text = "ICD-11: ${primary.icd11Primary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } ?: run {
                        Text(
                            text = "Healthy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else if (abnormalSegments.isEmpty() && overallDiagnosis.primaryDiagnosis?.id == 1) {
        Text(
            text = "All segments appear healthy",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension function to format Double to String with decimal places
private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}

@Composable
fun ProvisionalToothDiagnosisSection(state: DentalState) {
    var expanded by remember { mutableStateOf(false) }
    val diagnosticEngine = remember { ToothDiagnosticEngine() }
    
    // Exclude "not examined" and "missing" from abnormal list
    val abnormalTeeth = state.teeth.values.filter { tooth ->
        (tooth.status != "normal" && tooth.status != "not examined" && tooth.status != "missing") ||
        tooth.caries != "0 = ICDAS 0 - Sound tooth" ||
        tooth.fractureType != "0 = No fracture" ||
        tooth.erosion != "0 = No erosive wear" ||
        tooth.painStatus != "0 = No pain (asymptomatic)" ||
        tooth.tenderOnPercussion != "0 = Negative" ||
        tooth.vestibularTenderness != "0 = Negative" ||
        tooth.sinusTract != "0 = Normal (no sinus tract)"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExpandableSectionHeader(
                title = "Provisional Tooth Diagnosis",
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (abnormalTeeth.isEmpty()) {
                    Text("No abnormal teeth detected", style = MaterialTheme.typography.bodyLarge)
                } else {
                    abnormalTeeth.forEach { tooth ->
                        val separateResults = diagnosticEngine.getSeparateDiagnosisResults(tooth, state.patientInfo)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tooth ${tooth.number}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Feature Vector Display
                                var showVector by remember { mutableStateOf(false) }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Feature Vector (21 Parameters)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = { showVector = !showVector }) {
                                                Icon(
                                                    imageVector = if (showVector) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (showVector) "Collapse" else "Expand"
                                                )
                                            }
                                        }
                                        if (showVector) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = separateResults.featureVector.mapIndexed { index, value ->
                                                    val intValue = if (kotlin.math.abs(value % 1.0) < 0.001) value.toInt() else value
                                                    "P${index + 1}=$intValue"
                                                }.joinToString(", ", "[", "]"),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Final Integrated Diagnosis
                                separateResults.integratedDiagnosis?.let { integrated ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Final Provisional Diagnosis: ${integrated.name}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            val confidence = (integrated.cosineSimilarity * 0.50 + integrated.probability * 0.10 + (if (integrated.symbolicMatch) 1.0 else 0.0) * 0.40) * 100
                                            Text(
                                                text = "Confidence: ${confidence.format(1)}%",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            
                                            if (integrated.icd11Primary.isNotEmpty()) {
                                                Text(
                                                    text = "ICD-11: ${integrated.icd11Primary}${if (integrated.icd11Secondary.isNotEmpty()) " | ${integrated.icd11Secondary}" else ""}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Component Scores:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "📏 Distance: ${(integrated.cosineSimilarity * 100).format(1)}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "📊 Bayesian: ${(integrated.probability * 100).format(1)}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "🔍 Logic: ${if (integrated.symbolicMatch) "100.0%" else "0.0%"}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                } ?: run {
                                    Text(
                                        text = "Final Provisional Diagnosis: Healthy Tooth",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Differential Diagnoses in 3 Columns
                                Text(
                                    text = "Differential Diagnoses by Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Column 1: Distance-based Analysis
                                    Column(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "📏 Distance-Based",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "50% weight",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                DisplayDifferentialColumn(
                                                    results = separateResults.distanceResults,
                                                    getScore = { it.cosineSimilarity },
                                                    badgeColor = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Column 2: Bayesian Analysis
                                    Column(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "📊 Bayesian",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "10% weight",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                DisplayDifferentialColumn(
                                                    results = separateResults.bayesianResults,
                                                    getScore = { it.probability },
                                                    badgeColor = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Column 3: Symbolic Logic
                                    Column(modifier = Modifier.weight(1f)) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "🔍 Symbolic",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "40% weight",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (separateResults.symbolicResults.isEmpty()) {
                                                    Text(
                                                        text = "No matches",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                } else {
                                                    DisplayDifferentialColumn(
                                                        results = separateResults.symbolicResults,
                                                        getScore = { if (it.symbolicMatch) 1.0 else 0.0 },
                                                        badgeColor = MaterialTheme.colorScheme.tertiary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayDifferentialColumn(
    results: List<DiseaseVector>,
    getScore: (DiseaseVector) -> Double,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    if (results.isEmpty()) {
        Text(
            text = "No differential diagnoses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column {
            results.take(5).forEachIndexed { index, result ->
                val score = getScore(result)
                val percentage = (score * 100).format(1)
                val badgeClass = when {
                    percentage.toDouble() >= 70 -> "high"
                    percentage.toDouble() >= 40 -> "moderate"
                    else -> "low"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "${index + 1}. ${result.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.75f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.85f
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = badgeColor.copy(alpha = if (badgeClass == "high") 0.8f else if (badgeClass == "moderate") 0.6f else 0.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (result.icd11Primary.isNotEmpty()) {
                            Text(
                                text = "ICD-11: ${result.icd11Primary}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeparateDifferentialDiagnoses(differentials: List<*>) {
    // Separate differentials by type and sort them
    val byCosineSimilarity = differentials
        .filter { 
            when (it) {
                is DiseaseVector -> true
                is OralMucosaDiseaseVector -> true
                is MalocclusionDiseaseVector -> true
                else -> false
            }
        }
        .sortedByDescending {
            when (it) {
                is DiseaseVector -> it.cosineSimilarity
                is OralMucosaDiseaseVector -> it.cosineSimilarity
                is MalocclusionDiseaseVector -> it.cosineSimilarity
                else -> 0.0
            }
        }
    
    val byBayesianProbability = differentials.sortedByDescending {
        when (it) {
            is DiseaseVector -> it.probability
            is OralMucosaDiseaseVector -> it.probability
            is MalocclusionDiseaseVector -> it.probability
            is PeriodontalDiseaseVector -> it.probability
            else -> 0.0
        }
    }
    
    val bySymbolicLogic = differentials.filter {
        when (it) {
            is DiseaseVector -> it.symbolicMatch
            is OralMucosaDiseaseVector -> it.symbolicMatch
            is MalocclusionDiseaseVector -> it.symbolicMatch
            else -> false
        }
    }
    
    // Cosine Similarity Section
    if (byCosineSimilarity.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        ExpandableDifferentialSection(
            title = "Differential Diagnoses by Cosine Similarity",
            differentials = byCosineSimilarity,
            showMetric = { diff ->
                when (diff) {
                    is DiseaseVector -> "${(diff.cosineSimilarity * 100).format(1)}%"
                    is OralMucosaDiseaseVector -> "${(diff.cosineSimilarity * 100).format(1)}%"
                    is MalocclusionDiseaseVector -> "${(diff.cosineSimilarity * 100).format(1)}%"
                    else -> ""
                }
            }
        )
    }
    
    // Bayesian Probability Section
    if (byBayesianProbability.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        ExpandableDifferentialSection(
            title = "Differential Diagnoses by Bayesian Probability",
            differentials = byBayesianProbability,
            showMetric = { diff ->
                when (diff) {
                    is DiseaseVector -> "${(diff.probability * 100).format(1)}%"
                    is OralMucosaDiseaseVector -> "${(diff.probability * 100).format(1)}%"
                    is MalocclusionDiseaseVector -> "${(diff.probability * 100).format(1)}%"
                    is PeriodontalDiseaseVector -> "${(diff.probability * 100).format(1)}%"
                    else -> ""
                }
            }
        )
    }
    
    // Symbolic Logic Section
    if (bySymbolicLogic.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        ExpandableDifferentialSection(
            title = "Differential Diagnoses by Symbolic Logic (Matched)",
            differentials = bySymbolicLogic,
            showMetric = { "Matched" }
        )
    }
}

@Composable
fun ExpandableDifferentialSection(
    title: String,
    differentials: List<*>,
    showMetric: (Any) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
    }
    
    if (expanded) {
        Spacer(modifier = Modifier.height(8.dp))
        differentials.forEachIndexed { index, diff ->
            when (diff) {
                is DiseaseVector -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${index + 1}. ${diff.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Score: ${showMetric(diff)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Bayesian: ${(diff.probability * 100).format(1)}% | Cosine: ${(diff.cosineSimilarity * 100).format(1)}% | Symbolic: ${if (diff.symbolicMatch) "Yes" else "No"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (diff.icd11Primary.isNotEmpty()) {
                                Text(
                                    text = "ICD-11: ${diff.icd11Primary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                is OralMucosaDiseaseVector -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${index + 1}. ${diff.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Score: ${showMetric(diff)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Bayesian: ${(diff.probability * 100).format(1)}% | Cosine: ${(diff.cosineSimilarity * 100).format(1)}% | Symbolic: ${if (diff.symbolicMatch) "Yes" else "No"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (diff.icd11Primary.isNotEmpty()) {
                                Text(
                                    text = "ICD-11: ${diff.icd11Primary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                is MalocclusionDiseaseVector -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${index + 1}. ${diff.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Score: ${showMetric(diff)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Bayesian: ${(diff.probability * 100).format(1)}% | Cosine: ${(diff.cosineSimilarity * 100).format(1)}% | Symbolic: ${if (diff.symbolicMatch) "Yes" else "No"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (diff.icd11Primary.isNotEmpty()) {
                                Text(
                                    text = "ICD-11: ${diff.icd11Primary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                is PeriodontalDiseaseVector -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${index + 1}. ${diff.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Score: ${showMetric(diff)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Bayesian: ${(diff.probability * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (diff.matchedFeatures.isNotEmpty()) {
                                Text(
                                    text = "Matched Features: ${diff.matchedFeatures.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            if (diff.icd11Primary.isNotEmpty()) {
                                Text(
                                    text = "ICD-11: ${diff.icd11Primary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProvisionalPeriodontalDiagnosisSection(state: DentalState) {
    var expanded by remember { mutableStateOf(false) }
    val diagnosticEngine = remember { PeriodontalDiagnosticEngine() }
    
    val separateResults = diagnosticEngine.getSeparateDiagnosisResults(
        state.gingivalSegments,
        state.patientInfo,
        state.teeth
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExpandableSectionHeader(
                title = "Provisional Periodontal Diagnosis",
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Feature Vector Display
                var showVector by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Feature Vector (13 Parameters)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showVector = !showVector }) {
                                Icon(
                                    imageVector = if (showVector) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showVector) "Collapse" else "Expand"
                                )
                            }
                        }
                        if (showVector) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = separateResults.featureVector.toList().sortedBy { it.first }.map { (key, value) ->
                                    val intValue = if (kotlin.math.abs(value % 1.0) < 0.001) value.toInt() else value
                                    "P$key=$intValue"
                                }.joinToString(", ", "[", "]"),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Final Integrated Diagnosis
                separateResults.integratedDiagnosis?.let { integrated ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Final Provisional Diagnosis: ${integrated.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val confidence = (integrated.cosineSimilarity * 0.50 + integrated.probability * 0.10 + (if (integrated.symbolicMatch) 1.0 else 0.0) * 0.40) * 100
                            Text(
                                text = "Confidence: ${confidence.format(1)}%",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            if (integrated.icd11Primary.isNotEmpty()) {
                                Text(
                                    text = "ICD-11: ${integrated.icd11Primary}${if (integrated.icd11Secondary.isNotEmpty()) " | ${integrated.icd11Secondary}" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Component Scores:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "📏 Distance: ${(integrated.cosineSimilarity * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "📊 Bayesian: ${(integrated.probability * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "🔍 Logic: ${if (integrated.symbolicMatch) "100.0%" else "0.0%"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } ?: run {
                    Text(
                        text = "Final Provisional Diagnosis: Healthy Periodontium",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Differential Diagnoses in 3 Columns
                Text(
                    text = "Differential Diagnoses by Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Column 1: Distance-based Analysis
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "📏 Distance-Based",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "50% weight",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DisplayPeriodontalDifferentialColumn(
                                    results = separateResults.distanceResults,
                                    getScore = { it.cosineSimilarity },
                                    badgeColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Column 2: Bayesian Analysis
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "📊 Bayesian",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "10% weight",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DisplayPeriodontalDifferentialColumn(
                                    results = separateResults.bayesianResults,
                                    getScore = { it.probability },
                                    badgeColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    
                    // Column 3: Symbolic Logic
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "🔍 Symbolic",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "40% weight",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (separateResults.symbolicResults.isEmpty()) {
                                    Text(
                                        text = "No matches",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    DisplayPeriodontalDifferentialColumn(
                                        results = separateResults.symbolicResults,
                                        getScore = { if (it.symbolicMatch) 1.0 else 0.0 },
                                        badgeColor = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayPeriodontalDifferentialColumn(
    results: List<PeriodontalDiseaseVector>,
    getScore: (PeriodontalDiseaseVector) -> Double,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    if (results.isEmpty()) {
        Text(
            text = "No differential diagnoses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column {
            results.take(5).forEachIndexed { index, result ->
                val score = getScore(result)
                val percentage = (score * 100).format(1)
                val badgeClass = when {
                    percentage.toDouble() >= 70 -> "high"
                    percentage.toDouble() >= 40 -> "moderate"
                    else -> "low"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "${index + 1}. ${result.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.75f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.85f
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = badgeColor.copy(alpha = if (badgeClass == "high") 0.8f else if (badgeClass == "moderate") 0.6f else 0.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (result.icd11Primary.isNotEmpty()) {
                            Text(
                                text = "ICD-11: ${result.icd11Primary}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayOralMucosaDifferentialColumn(
    results: List<OralMucosaDiseaseVector>,
    getScore: (OralMucosaDiseaseVector) -> Double,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    if (results.isEmpty()) {
        Text(
            text = "No differential diagnoses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column {
            results.take(5).forEachIndexed { index, result ->
                val score = getScore(result)
                val percentage = (score * 100).format(1)
                val badgeClass = when {
                    percentage.toDouble() >= 70 -> "high"
                    percentage.toDouble() >= 40 -> "moderate"
                    else -> "low"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "${index + 1}. ${result.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.75f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.85f
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = badgeColor.copy(alpha = if (badgeClass == "high") 0.8f else if (badgeClass == "moderate") 0.6f else 0.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (result.icd11Primary.isNotEmpty() && result.icd11Primary != "None") {
                            Text(
                                text = "ICD-11: ${result.icd11Primary}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayMalocclusionDifferentialColumn(
    results: List<MalocclusionDiseaseVector>,
    getScore: (MalocclusionDiseaseVector) -> Double,
    badgeColor: androidx.compose.ui.graphics.Color
) {
    if (results.isEmpty()) {
        Text(
            text = "No differential diagnoses",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Column {
            results.take(5).forEachIndexed { index, result ->
                val score = getScore(result)
                val percentage = (score * 100).format(1)
                val badgeClass = when {
                    percentage.toDouble() >= 70 -> "high"
                    percentage.toDouble() >= 40 -> "moderate"
                    else -> "low"
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "${index + 1}. ${result.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.75f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 0.85f
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = badgeColor.copy(alpha = if (badgeClass == "high") 0.8f else if (badgeClass == "moderate") 0.6f else 0.4f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (result.icd11Primary.isNotEmpty() && result.icd11Primary != "None") {
                            Text(
                                text = "ICD-11: ${result.icd11Primary}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.7f),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProvisionalOralMucosaDiagnosisSection(state: DentalState) {
    var expanded by remember { mutableStateOf(false) }
    val diagnosticEngine = remember { OralMucosaDiagnosticEngine() }
    
    val diagnosis = diagnosticEngine.diagnose(state.patientInfo)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExpandableSectionHeader(
                title = "Provisional Oral Mucosa Diagnosis",
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                diagnosis.primaryDiagnosis?.let { primary ->
                    // Final Integrated Diagnosis Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Final Provisional Diagnosis: ${primary.name}",
                                style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                            
                            val confidence = (primary.cosineSimilarity * 0.50 + primary.probability * 0.10 + (if (primary.symbolicMatch) 1.0 else 0.0) * 0.40) * 100
                    Text(
                                text = "Confidence: ${confidence.format(1)}%",
                                style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                            
                            if (primary.icd11Primary.isNotEmpty() && primary.icd11Primary != "None") {
                    Text(
                                    text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) " | ${primary.icd11Secondary}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                    )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                    Text(
                                text = "Component Scores:",
                                style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                        Text(
                                text = "📏 Distance: ${(primary.cosineSimilarity * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "📊 Bayesian: ${(primary.probability * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "🔍 Logic: ${if (primary.symbolicMatch) "100.0%" else "0.0%"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Differential Diagnoses in 3 Columns
                    if (diagnosis.differentialDiagnoses.isNotEmpty()) {
                        Text(
                            text = "Differential Diagnoses by Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Group differentials by method
                        val distanceResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<OralMucosaDiseaseVector>()
                            .sortedByDescending { it.cosineSimilarity }
                            .take(5)
                        val bayesianResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<OralMucosaDiseaseVector>()
                            .sortedByDescending { it.probability }
                            .take(5)
                        val symbolicResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<OralMucosaDiseaseVector>()
                            .filter { it.symbolicMatch }
                            .take(5)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1: Distance-based Analysis
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "📏 Distance-Based",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "50% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        DisplayOralMucosaDifferentialColumn(
                                            results = distanceResults,
                                            getScore = { it.cosineSimilarity },
                                            badgeColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            // Column 2: Bayesian Analysis
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "📊 Bayesian",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "10% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        DisplayOralMucosaDifferentialColumn(
                                            results = bayesianResults,
                                            getScore = { it.probability },
                                            badgeColor = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                            
                            // Column 3: Symbolic Logic
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "🔍 Symbolic",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "40% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (symbolicResults.isEmpty()) {
                                            Text(
                                                text = "No matches",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            DisplayOralMucosaDifferentialColumn(
                                                results = symbolicResults,
                                                getScore = { if (it.symbolicMatch) 1.0 else 0.0 },
                                                badgeColor = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "Final Provisional Diagnosis: Normal Oral Mucosa",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ProvisionalMalocclusionDiagnosisSection(state: DentalState) {
    var expanded by remember { mutableStateOf(false) }
    val diagnosticEngine = remember { MalocclusionDiagnosticEngine() }
    
    val diagnosis = diagnosticEngine.diagnose(state.patientInfo)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExpandableSectionHeader(
                title = "Provisional Malocclusion Diagnosis",
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                diagnosis.primaryDiagnosis?.let { primary ->
                    // Final Integrated Diagnosis Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Final Provisional Diagnosis: ${primary.name}",
                                style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                            
                            val confidence = (primary.cosineSimilarity * 0.50 + primary.probability * 0.10 + (if (primary.symbolicMatch) 1.0 else 0.0) * 0.40) * 100
                    Text(
                                text = "Confidence: ${confidence.format(1)}%",
                                style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                            
                            if (primary.icd11Primary.isNotEmpty() && primary.icd11Primary != "None") {
                    Text(
                                    text = "ICD-11: ${primary.icd11Primary}${if (primary.icd11Secondary.isNotEmpty()) " | ${primary.icd11Secondary}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                    )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                    Text(
                                text = "Component Scores:",
                                style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                        Text(
                                text = "📏 Distance: ${(primary.cosineSimilarity * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "📊 Bayesian: ${(primary.probability * 100).format(1)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "🔍 Logic: ${if (primary.symbolicMatch) "100.0%" else "0.0%"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Differential Diagnoses in 3 Columns
                    if (diagnosis.differentialDiagnoses.isNotEmpty()) {
                        Text(
                            text = "Differential Diagnoses by Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Group differentials by method
                        val distanceResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<MalocclusionDiseaseVector>()
                            .sortedByDescending { it.cosineSimilarity }
                            .take(5)
                        val bayesianResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<MalocclusionDiseaseVector>()
                            .sortedByDescending { it.probability }
                            .take(5)
                        val symbolicResults = diagnosis.differentialDiagnoses
                            .filterIsInstance<MalocclusionDiseaseVector>()
                            .filter { it.symbolicMatch }
                            .take(5)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1: Distance-based Analysis
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "📏 Distance-Based",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "50% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        DisplayMalocclusionDifferentialColumn(
                                            results = distanceResults,
                                            getScore = { it.cosineSimilarity },
                                            badgeColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            // Column 2: Bayesian Analysis
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "📊 Bayesian",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "10% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        DisplayMalocclusionDifferentialColumn(
                                            results = bayesianResults,
                                            getScore = { it.probability },
                                            badgeColor = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                            
                            // Column 3: Symbolic Logic
                            Column(modifier = Modifier.weight(1f)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "🔍 Symbolic",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "40% weight",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (symbolicResults.isEmpty()) {
                                            Text(
                                                text = "No matches",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            DisplayMalocclusionDifferentialColumn(
                                                results = symbolicResults,
                                                getScore = { if (it.symbolicMatch) 1.0 else 0.0 },
                                                badgeColor = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "Final Provisional Diagnosis: Normal Occlusion",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodontalMatrixTable(segments: Map<Int, GingivalSegment>) {
    val parameterLabels = listOf(
        "Gingival Color",
        "Gingival Size",
        "Shape",
        "Contour",
        "Texture",
        "Consistency",
        "Distribution",
        "Bleeding",
        "Exudate",
        "Calculus",
        "Plaque"
    )
    
    val getParameterValue: (GingivalSegment, Int) -> String = { segment, index ->
        when (index) {
            0 -> segment.color
            1 -> segment.sizeEdema
            2 -> segment.shape
            3 -> segment.contour
            4 -> segment.texture
            5 -> segment.consistency
            6 -> segment.distribution
            7 -> segment.bleeding
            8 -> segment.exudate
            9 -> segment.calculus
            10 -> segment.plaque
            else -> ""
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // First column for parameter labels
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = androidx.compose.ui.Alignment.CenterStart
                ) {
                    Text(
                        "Parameter",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Segment columns
                (1..6).forEach { segmentNum ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            "Segment $segmentNum",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Divider(thickness = 1.dp)
            
            // Data rows
            parameterLabels.forEachIndexed { paramIndex, paramLabel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Parameter label column
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = androidx.compose.ui.Alignment.CenterStart
                    ) {
                        Text(
                            paramLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Segment value columns
                    (1..6).forEach { segmentNum ->
                        val segment = segments[segmentNum]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                segment?.let { getParameterValue(it, paramIndex) } ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (paramIndex < parameterLabels.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun AnomalySummaryItem(label: String, teeth: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = teeth,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(label: String, options: List<String>, selectedOption: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        expanded = false
                        onSelected(selectionOption)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    patientRepository: PatientRepository,
    onPatientSelected: (Patient) -> Unit,
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    context: Any? = null,
    authState: AuthState = AuthState(),
    userTrackingService: UserTrackingService? = null,
    blockingService: BlockingService? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val patientsState by patientRepository.patients
    val patients = remember(searchQuery, patientsState) {
        if (searchQuery.isBlank()) {
            patientRepository.getRecentPatients(50)
        } else {
            patientRepository.searchPatients(searchQuery)
        }
    }
    
    var showAddPatientDialog by remember { mutableStateOf(false) }
    var patientToEdit by remember { mutableStateOf<Patient?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Patient?>(null) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var showTrackedUsersDialog by remember { mutableStateOf(false) }
    var trackedUsers by remember { mutableStateOf<List<TrackedUser>>(emptyList()) }
    var isLoadingTrackedUsers by remember { mutableStateOf(false) }
    
    // Check if current user is admin
    val isAdmin = authState.userEmail == "sriramreddydwarampudi@gmail.com"
    
    // Initialize PDF generator and share helper
    val pdfGenerator = remember(context) { createPdfReportGenerator(context) }
    val shareHelper = remember(context) { createShareHelper(context) }
    val reportSharingHelper = rememberReportSharingHelper(pdfGenerator, shareHelper, context)
    val csvExportHelper = remember { CsvExportHelper() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Patients",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Export to CSV button
                        IconButton(
                            onClick = {
                                isExporting = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        val allPatients = patientRepository.getAllPatients()
                                        if (allPatients.isNotEmpty()) {
                                            val csvContent = csvExportHelper.exportPatientsToCsv(allPatients)
                                            val fileName = csvExportHelper.generateCsvFileName()
                                            shareHelper.shareCsvFile(csvContent, fileName)
                                            syncMessage = "Exported ${allPatients.size} patients to CSV"
                                        } else {
                                            syncMessage = "No patients to export"
                                        }
                                    } catch (e: Exception) {
                                        syncMessage = "Export failed: ${e.message}"
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = androidx.compose.ui.graphics.Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Export to CSV",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        
                        // Dark mode toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = if (isDarkMode) "Light Mode" else "Dark Mode",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = onDarkModeToggle,
                                modifier = Modifier.size(width = 36.dp, height = 20.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                                    uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPatientDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Patient",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Sync message
            syncMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("failed", ignoreCase = true) || message.contains("error", ignoreCase = true)) 
                            MaterialTheme.colorScheme.errorContainer 
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search patients...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { /* Search is handled automatically */ }
                )
            )
            
            // Recent Patients Header
            if (searchQuery.isBlank()) {
                Text(
                    "Recent Patients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    "Search Results (${patients.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Patient List
            if (patients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No patients yet" else "No patients found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (searchQuery.isBlank()) 
                                "Tap the + button to add a new patient" 
                            else "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(patients.size) { index ->
                        val patient = patients[index]
                        PatientCard(
                            patient = patient,
                            onClick = { onPatientSelected(patient) },
                            onEdit = { patientToEdit = patient },
                            onDelete = { showDeleteDialog = patient },
                            onShare = { 
                                reportSharingHelper.sharePatientReport(patient)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add/Edit Patient Dialog
    if (showAddPatientDialog || patientToEdit != null) {
        PatientDialog(
            patient = patientToEdit,
            onDismiss = {
                showAddPatientDialog = false
                patientToEdit = null
            },
            onSave = { patient ->
                if (patientToEdit != null) {
                    patientRepository.updatePatient(patient)
                } else {
                    patientRepository.addPatient(patient)
                }
                showAddPatientDialog = false
                patientToEdit = null
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteDialog?.let { patient ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Patient") },
            text = { Text("Are you sure you want to delete ${patient.displayName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        patientRepository.deletePatient(patient.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Tracked Users Dialog (Admin only)
    if (showTrackedUsersDialog) {
        AlertDialog(
            onDismissRequest = { showTrackedUsersDialog = false },
            title = { 
                Text(
                    "Tracked Users",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (isLoadingTrackedUsers) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (trackedUsers.isEmpty()) {
                    Text("No tracked users found.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trackedUsers) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (user.name != null && user.name.isNotEmpty()) {
                                        Text(
                                            text = "Name: ${user.name}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        text = "Device: ${user.deviceName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Device ID: ${user.deviceId.take(20)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Sessions: ${user.sessionCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Last Seen: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(user.lastSeen))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTrackedUsersDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = patient.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ID: ${patient.id}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (patient.phoneNumber.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = patient.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (patient.email.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = patient.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Created: ${patient.createdDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Updated: ${patient.updatedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PatientDialog(
    patient: Patient?,
    onDismiss: () -> Unit,
    onSave: (Patient) -> Unit
) {
    var surname by remember { mutableStateOf(patient?.surname ?: "") }
    var givenName by remember { mutableStateOf(patient?.givenName ?: "") }
    var phoneNumber by remember { mutableStateOf(patient?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(patient?.email ?: "") }
    var address by remember { mutableStateOf(patient?.address ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient == null) "Add New Patient" else "Edit Patient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = givenName,
                    onValueChange = { givenName = it },
                    label = { Text("Given Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedPatient = (patient ?: Patient()).copy(
                        surname = surname.trim(),
                        givenName = givenName.trim(),
                        phoneNumber = phoneNumber.trim(),
                        email = email.trim(),
                        address = address.trim()
                    )
                    onSave(updatedPatient)
                },
                enabled = surname.trim().isNotEmpty() || givenName.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

