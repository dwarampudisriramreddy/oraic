package com.ram.orai.oraic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6), // Blue 300
    secondary = Color(0xFF42A5F5), // Blue 400
    tertiary = Color(0xFF90CAF9) // Blue 200
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2), // Blue 700
    secondary = Color(0xFF1565C0), // Blue 800
    tertiary = Color(0xFF0D47A1) // Blue 900
)

expect fun getDatabaseDriverFactory(): DatabaseDriverFactory
expect fun getAppContext(): Any?

// Simple device info for tracking
expect fun getDeviceId(context: Any?): String
expect fun getDeviceName(context: Any?): String

// Blocking service for admin controls (no-op after Firebase removal)
expect class BlockingService {
    suspend fun isEmailBlocked(email: String): Result<Boolean>
    suspend fun isDeviceBlocked(deviceId: String): Result<Boolean>
    suspend fun checkDeviceLimit(email: String, deviceId: String): Result<Boolean>
}

expect fun createBlockingService(context: Any?): BlockingService?

@Composable
fun App(
    driverFactory: DatabaseDriverFactory? = null,
    pdfGenerator: PdfReportGenerator? = null,
    shareHelper: ShareHelper? = null,
    context: Any? = null
) {
    val systemDarkMode = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkMode) }
    val factory = driverFactory ?: remember { getDatabaseDriverFactory() }
    val databaseHelper = remember(factory) { DatabaseHelper(factory) }
    val patientRepository = remember(databaseHelper) { PatientRepository(databaseHelper) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    
    val appContext = context ?: getAppContext()
    
    // User tracking service
    val userTrackingService = remember(appContext) { createUserTrackingService(appContext) }
    
    // Blocking service for admin controls
    val blockingService = remember(appContext) { createBlockingService(appContext) }
    
    // Auth state - always signed in (no authentication required)
    val authState = AuthState(isSignedIn = true)
    
    MaterialTheme(
        colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    ) {
        // Show main app (no authentication required)
        MainAppContent(
            selectedPatient = selectedPatient,
            patientRepository = patientRepository,
            onPatientSelected = { selectedPatient = it },
            onClearSelectedPatient = { selectedPatient = null },
            isDarkMode = isDarkMode,
            onDarkModeToggle = { isDarkMode = it },
            appContext = appContext,
            authState = authState,
            userTrackingService = userTrackingService,
            blockingService = blockingService
        )
    }
}

@Composable
private fun MainAppContent(
    selectedPatient: Patient?,
    patientRepository: PatientRepository,
    onPatientSelected: (Patient) -> Unit,
    onClearSelectedPatient: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    appContext: Any?,
    authState: AuthState,
    userTrackingService: UserTrackingService? = null,
    blockingService: BlockingService? = null
) {
    if (selectedPatient == null) {
        // Show Dashboard
        DashboardScreen(
            patientRepository = patientRepository,
            onPatientSelected = onPatientSelected,
                isDarkMode = isDarkMode,
                onDarkModeToggle = { onDarkModeToggle(it) },
                context = appContext,
                authState = authState,
                userTrackingService = userTrackingService,
                blockingService = blockingService
            )
        } else {
            // Show Patient Detail Screens
            val stateHolder = remember(selectedPatient!!.id) { 
                DentalStateHolder().apply {
                    // Load patient's dental state
                    initializeWithDentalState(selectedPatient!!.dentalState)
                }
            }
            
            MainScreen(
                stateHolder = stateHolder,
                patient = selectedPatient!!,
                patientRepository = patientRepository,
                    onBackToDashboard = {
                        // Save patient's dental state before going back
                        val updatedPatient = selectedPatient!!.copy(
                            dentalState = stateHolder.uiState,
                            updatedAt = System.currentTimeMillis() // Update timestamp
                        )
                        patientRepository.updatePatient(updatedPatient)
                        onClearSelectedPatient()
                    },
                isDarkMode = isDarkMode,
                onDarkModeToggle = { onDarkModeToggle(it) }
            )
        }
}