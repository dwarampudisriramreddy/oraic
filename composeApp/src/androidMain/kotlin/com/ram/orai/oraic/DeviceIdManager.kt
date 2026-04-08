package com.ram.orai.oraic

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * Manages unique device ID generation for device approval system
 */
object DeviceIdManager {
    /**
     * Generates a unique, stable device ID for this Android device
     * Uses Android ID (stable across app reinstalls) combined with device info
     */
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val deviceInfo = "${android.os.Build.MANUFACTURER}_${android.os.Build.MODEL}_${android.os.Build.SERIAL}"
        val combined = "${androidId}_$deviceInfo"
        
        // Create SHA-256 hash for privacy and consistency
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Gets a user-friendly device name for display
     */
    fun getDeviceName(context: Context): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        return "$manufacturer $model"
    }
}

