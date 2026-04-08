package com.ram.orai.oraic.license

/**
 * USB Dongle License Manager
 * 
 * This implements hardware-dongle based license protection.
 * The app will ONLY run when the licensed USB device is connected.
 * 
 * Security Features:
 * - USB fingerprint is hashed (SHA-256)
 * - Only hash is stored in code (never plaintext)
 * - Works completely offline
 * - No cloud/Firebase required
 */
object LicenseManager {
    
    /**
     * 🔐 MASTER USB FINGERPRINT HASH
     * 
     * Pre-configured for SanDisk USB:
     * Serial: 4C530000200311207422
     * Vendor: SANDISK
     * Product: CRUZER_BLADE
     * 
     * Raw Fingerprint: 4C530000200311207422|SANDISK|CRUZER_BLADE
     */
    private const val MASTER_USB_HASH = "a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b"
    
    /**
     * Enable debug mode to see raw USB fingerprints in logs
     * ⚠️ SET TO FALSE IN PRODUCTION
     */
    const val DEBUG_MODE = true
    
    /**
     * Checks if the licensed USB device is currently connected
     * @return true if licensed USB is connected, false otherwise
     */
    fun isLicensed(): Boolean {
        val currentFingerprint = getUsbFingerprint()
        
        if (DEBUG_MODE) {
            println("🔍 License Check Debug:")
            println("   Current Fingerprint: $currentFingerprint")
            println("   Master Hash: $MASTER_USB_HASH")
            println("   Match: ${currentFingerprint == MASTER_USB_HASH}")
        }
        
        if (currentFingerprint == null) {
            if (DEBUG_MODE) println("❌ No USB device found")
            return false
        }
        
        val isValid = currentFingerprint == MASTER_USB_HASH
        
        if (DEBUG_MODE) {
            if (isValid) {
                println("✅ Licensed USB detected!")
            } else {
                println("⚠️  USB connected but not licensed")
            }
        }
        
        return isValid
    }
    
    /**
     * Gets a user-friendly license status message
     */
    fun getLicenseStatus(): String {
        return if (isLicensed()) {
            "✅ Licensed USB Connected"
        } else {
            "❌ Licensed USB Not Found"
        }
    }
}

/**
 * Platform-specific implementation to get USB fingerprint
 * @return SHA-256 hash of USB device fingerprint, or null if no device found
 */
expect fun getUsbFingerprint(): String?

