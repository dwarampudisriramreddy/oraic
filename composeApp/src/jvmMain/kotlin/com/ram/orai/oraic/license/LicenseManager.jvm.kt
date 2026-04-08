package com.ram.orai.oraic.license

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * Desktop (Windows/Linux/macOS) USB fingerprint implementation
 */

actual fun getUsbFingerprint(): String? {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    
    if (LicenseManager.DEBUG_MODE) {
        println("🖥️  Operating System: $os")
    }
    
    return when {
        os.contains("win") -> getWindowsUsbFingerprint()
        os.contains("nux") -> getLinuxUsbFingerprint()
        os.contains("mac") || os.contains("darwin") -> getMacOsUsbFingerprint()
        else -> {
            println("⚠️  Unsupported OS: $os")
            null
        }
    }
}

/**
 * Windows USB fingerprint using WMIC
 */
private fun getWindowsUsbFingerprint(): String? {
    try {
        if (LicenseManager.DEBUG_MODE) {
            println("🔍 Scanning Windows USB devices...")
        }
        
        val process = ProcessBuilder(
            "wmic",
            "diskdrive",
            "get",
            "serialnumber,model,pnpdeviceid"
        ).redirectErrorStream(true).start()
        
        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }
        
        process.waitFor()
        
        if (LicenseManager.DEBUG_MODE) {
            println("📝 WMIC Output:")
            println(output)
        }
        
        // Parse output for USB devices
        val lines = output.lines()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty() || line.startsWith("Model")) continue
            
            // Look for lines containing USB identifiers
            if (line.contains("USB", ignoreCase = true) || line.contains("USBSTOR", ignoreCase = true)) {
                // Try to extract serial number from various formats
                val serialRegex = Regex("([A-Z0-9]{10,})")
                val serialMatches = serialRegex.findAll(line).toList()
                
                for (match in serialMatches) {
                    val serial = match.groupValues[1]
                    
                    // Look for vendor/product in the line
                    val vendor = when {
                        line.contains("SanDisk", ignoreCase = true) -> "SANDISK"
                        line.contains("Kingston", ignoreCase = true) -> "KINGSTON"
                        else -> "USB"
                    }
                    
                    val product = when {
                        line.contains("Cruzer", ignoreCase = true) -> "CRUZER_BLADE"
                        else -> "STORAGE"
                    }
                    
                    val rawFingerprint = "$serial|$vendor|$product"
                    
                    if (LicenseManager.DEBUG_MODE) {
                        println("🔑 Raw Fingerprint: $rawFingerprint")
                    }
                    
                    return Sha256Util.hash(rawFingerprint)
                }
            }
        }
        
        // Fallback: Try PowerShell method
        return getWindowsUsbFingerprintPowerShell()
        
    } catch (e: Exception) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ Windows USB detection error: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}

/**
 * Windows PowerShell fallback method
 */
private fun getWindowsUsbFingerprintPowerShell(): String? {
    try {
        val process = ProcessBuilder(
            "powershell",
            "-Command",
            "Get-Disk | Where-Object {\$_.BusType -eq 'USB'} | Select-Object SerialNumber, Model, FriendlyName"
        ).redirectErrorStream(true).start()
        
        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }
        
        process.waitFor()
        
        if (LicenseManager.DEBUG_MODE) {
            println("📝 PowerShell Output:")
            println(output)
        }
        
        // Extract serial number
        val serialRegex = Regex("SerialNumber\\s*:\\s*([A-Z0-9]+)", RegexOption.IGNORE_CASE)
        val serialMatch = serialRegex.find(output)
        
        if (serialMatch != null) {
            val serial = serialMatch.groupValues[1].trim()
            
            // Extract vendor/product from Model or FriendlyName
            val vendor = when {
                output.contains("SanDisk", ignoreCase = true) -> "SANDISK"
                output.contains("Kingston", ignoreCase = true) -> "KINGSTON"
                else -> "USB"
            }
            
            val product = when {
                output.contains("Cruzer", ignoreCase = true) -> "CRUZER_BLADE"
                else -> "STORAGE"
            }
            
            val rawFingerprint = "$serial|$vendor|$product"
            
            if (LicenseManager.DEBUG_MODE) {
                println("🔑 Raw Fingerprint: $rawFingerprint")
            }
            
            return Sha256Util.hash(rawFingerprint)
        }
        
        return null
        
    } catch (e: Exception) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ PowerShell USB detection error: ${e.message}")
        }
        return null
    }
}

/**
 * Linux USB fingerprint using /dev/disk/by-id or lsusb
 */
private fun getLinuxUsbFingerprint(): String? {
    try {
        if (LicenseManager.DEBUG_MODE) {
            println("🔍 Scanning Linux USB devices...")
        }
        
        // Method 1: Read /dev/disk/by-id/
        val diskByIdPath = "/dev/disk/by-id/"
        val diskByIdDir = java.io.File(diskByIdPath)
        
        if (diskByIdDir.exists()) {
            val usbDevices = diskByIdDir.listFiles { file ->
                file.name.startsWith("usb-")
            }
            
            if (LicenseManager.DEBUG_MODE) {
                println("📝 Found ${usbDevices?.size ?: 0} USB devices in $diskByIdPath")
                usbDevices?.forEach { println("   - ${it.name}") }
            }
            
            // Take first USB device found
            val usbDevice = usbDevices?.firstOrNull()
            
            if (usbDevice != null) {
                // Extract info from filename
                val nameRegex = Regex("usb-([^_]+)_([^_]+)_([^-]+)")
                val match = nameRegex.find(usbDevice.name)
                
                val rawFingerprint = if (match != null) {
                    "${match.groupValues[3]}|${match.groupValues[1]}|${match.groupValues[2]}"
                } else {
                    usbDevice.name.replace("usb-", "")
                }
                
                if (LicenseManager.DEBUG_MODE) {
                    println("🔑 Raw Fingerprint: $rawFingerprint")
                }
                
                return Sha256Util.hash(rawFingerprint)
            }
        }
        
        // Method 2: Use lsusb command
        return getLinuxUsbFingerprintLsusb()
        
    } catch (e: Exception) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ Linux USB detection error: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}

/**
 * Linux lsusb fallback method
 */
private fun getLinuxUsbFingerprintLsusb(): String? {
    try {
        val process = ProcessBuilder("lsusb", "-v")
            .redirectErrorStream(true)
            .start()
        
        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }
        
        process.waitFor()
        
        if (LicenseManager.DEBUG_MODE) {
            println("📝 lsusb output (truncated):")
            println(output.take(500))
        }
        
        // Look for any USB storage device
        val serialRegex = Regex("iSerial.*?\\s+([A-Z0-9]+)")
        val match = serialRegex.find(output)
        
        if (match != null) {
            val serial = match.groupValues[1]
            val rawFingerprint = "$serial|USB|STORAGE"
            
            if (LicenseManager.DEBUG_MODE) {
                println("🔑 Raw Fingerprint: $rawFingerprint")
            }
            
            return Sha256Util.hash(rawFingerprint)
        }
        
        return null
        
    } catch (e: Exception) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ lsusb error: ${e.message}")
        }
        return null
    }
}

/**
 * macOS USB fingerprint using system_profiler
 */
private fun getMacOsUsbFingerprint(): String? {
    try {
        if (LicenseManager.DEBUG_MODE) {
            println("🔍 Scanning macOS USB devices...")
        }
        
        val process = ProcessBuilder(
            "system_profiler",
            "SPUSBDataType"
        ).redirectErrorStream(true).start()
        
        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }
        
        process.waitFor()
        
        if (LicenseManager.DEBUG_MODE) {
            println("📝 system_profiler output (truncated):")
            println(output.take(1000))
        }
        
        // Look for USB device with serial
        val lines = output.lines()
        var serial: String? = null
        var vendor = "USB"
        var product = "STORAGE"
        
        for (i in lines.indices) {
            val line = lines[i]
            
            // Look for vendor
            if (line.contains("Manufacturer:", ignoreCase = true)) {
                if (line.contains("SanDisk", ignoreCase = true)) {
                    vendor = "SANDISK"
                }
            }
            
            // Look for product
            if (line.contains("Product ID:", ignoreCase = true) || line.contains(":", ignoreCase = true)) {
                if (line.contains("Cruzer", ignoreCase = true)) {
                    product = "CRUZER_BLADE"
                }
            }
            
            // Look for serial
            if (line.contains("Serial Number:", ignoreCase = true)) {
                val serialRegex = Regex("Serial Number:\\s*([A-Z0-9]+)")
                val match = serialRegex.find(line)
                serial = match?.groupValues?.get(1)?.trim()
                if (serial != null && serial.isNotEmpty()) break
            }
        }
        
        if (serial != null && serial.isNotEmpty()) {
            val rawFingerprint = "$serial|$vendor|$product"
            
            if (LicenseManager.DEBUG_MODE) {
                println("🔑 Raw Fingerprint: $rawFingerprint")
            }
            
            return Sha256Util.hash(rawFingerprint)
        }
        
        return null
        
    } catch (e: Exception) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ macOS USB detection error: ${e.message}")
            e.printStackTrace()
        }
        return null
    }
}



