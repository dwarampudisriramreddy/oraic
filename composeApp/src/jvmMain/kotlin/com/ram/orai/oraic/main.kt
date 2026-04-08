package com.ram.orai.oraic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ram.orai.oraic.license.LicenseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

fun main() = application {
    // 🔐 License check state
    var isLicensed by remember { mutableStateOf(false) }
    var isCheckingLicense by remember { mutableStateOf(true) }
    var licenseStatus by remember { mutableStateOf("Checking...") }
    
    // 🔐 Check license on startup
    LaunchedEffect(Unit) {
        println("🔐 Checking license on startup...")
        isLicensed = LicenseManager.isLicensed()
        licenseStatus = LicenseManager.getLicenseStatus()
        isCheckingLicense = false
        
        // 🔐 Runtime USB removal detection (poll every 3 seconds)
        if (isLicensed) {
            launch {
                while (true) {
                    delay(3000)
                    val stillLicensed = LicenseManager.isLicensed()
                    
                    if (!stillLicensed && isLicensed) {
                        println("⚠️  USB removed - locking application")
                        isLicensed = false
                        licenseStatus = "❌ USB Removed - App Locked"
                    } else if (stillLicensed && !isLicensed) {
                        println("✅ USB reconnected - unlocking application")
                        isLicensed = true
                        licenseStatus = LicenseManager.getLicenseStatus()
                    }
                }
            }
        }
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "ORAIC",
    ) {
        MaterialTheme {
            when {
                isCheckingLicense -> LicenseCheckScreenDesktop()
                !isLicensed -> LicenseBlockedScreenDesktop(
                    status = licenseStatus,
                    onRetry = {
                        isCheckingLicense = true
                        Thread {
                            Thread.sleep(500)
                            isLicensed = LicenseManager.isLicensed()
                            licenseStatus = LicenseManager.getLicenseStatus()
                            isCheckingLicense = false
                        }.start()
                    },
                    onExit = { exitProcess(0) }
                )
                else -> {
                    // Desktop: No cloud functionality
                    App()
                }
            }
        }
    }
}

// 🔐 License UI Screens for Desktop
@Composable
fun LicenseCheckScreenDesktop() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("🔐 Checking License", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Verifying USB dongle...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun LicenseBlockedScreenDesktop(status: String, onRetry: () -> Unit, onExit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🔒", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Licensed USB Not Found", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(status, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "This application requires a licensed USB device to run.\n\nPlease connect the authorized USB dongle and click Retry.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onExit, modifier = Modifier.width(140.dp).height(48.dp)) {
                    Text("Exit")
                }
                Button(onClick = onRetry, modifier = Modifier.width(140.dp).height(48.dp)) {
                    Text("Retry")
                }
            }
        }
    }
}





























