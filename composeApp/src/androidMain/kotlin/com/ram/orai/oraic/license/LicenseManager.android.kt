package com.ram.orai.oraic.license

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

/**
 * Android-specific USB fingerprint implementation
 */

private lateinit var appContext: Context

/**
 * Initialize the license manager with application context
 * ⚠️ MUST be called from MainActivity.onCreate()
 */
fun initLicenseManager(context: Context) {
    appContext = context.applicationContext
}

actual fun getUsbFingerprint(): String? {
    if (!::appContext.isInitialized) {
        println("⚠️  LicenseManager not initialized! Call initLicenseManager() first")
        return null
    }
    
    val usbManager = appContext.getSystemService(Context.USB_SERVICE) as? UsbManager
    if (usbManager == null) {
        if (LicenseManager.DEBUG_MODE) {
            println("❌ UsbManager not available")
        }
        return null
    }
    
    val deviceList = usbManager.deviceList
    if (LicenseManager.DEBUG_MODE) {
        println("🔍 Found ${deviceList.size} USB devices")
    }
    
    // Look for USB mass storage devices
    for (device in deviceList.values) {
        val fingerprint = generateFingerprint(device)
        if (fingerprint != null) {
            return fingerprint
        }
    }
    
    return null
}

/**
 * Generates a unique fingerprint for a USB device
 */
private fun generateFingerprint(device: UsbDevice): String? {
    val serial = device.serialNumber
    val vendorId = device.vendorId
    val productId = device.productId
    val deviceName = device.deviceName
    val manufacturerName = device.manufacturerName
    val productName = device.productName
    
    if (LicenseManager.DEBUG_MODE) {
        println("📱 USB Device:")
        println("   Serial: $serial")
        println("   Vendor ID: ${vendorId.toString(16).uppercase()}")
        println("   Product ID: ${productId.toString(16).uppercase()}")
        println("   Device Name: $deviceName")
        println("   Manufacturer: $manufacturerName")
        println("   Product: $productName")
    }
    
    // Skip devices without serial numbers
    if (serial.isNullOrEmpty()) {
        if (LicenseManager.DEBUG_MODE) {
            println("   ⏩ Skipped (no serial)")
        }
        return null
    }
    
    // Create raw fingerprint string
    val manufacturer = (manufacturerName ?: "UNKNOWN").replace(" ", "_").uppercase()
    val product = (productName ?: "UNKNOWN").replace(" ", "_").uppercase()
    
    val rawFingerprint = "$serial|$manufacturer|$product"
    
    if (LicenseManager.DEBUG_MODE) {
        println("🔑 Raw Fingerprint: $rawFingerprint")
        println("   (Use this to generate MASTER_USB_HASH)")
    }
    
    // Return SHA-256 hash
    val hash = Sha256Util.hash(rawFingerprint)
    
    if (LicenseManager.DEBUG_MODE) {
        println("🔐 SHA-256 Hash: $hash")
    }
    
    return hash
}



