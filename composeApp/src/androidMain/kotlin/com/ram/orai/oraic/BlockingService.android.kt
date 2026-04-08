package com.ram.orai.oraic

import android.content.Context
import android.util.Log

private const val TAG = "BlockingService"

actual class BlockingService(private val context: Context) {
    /**
     * Check if an email is blocked - always returns false (no blocking after Firebase removal)
     */
    actual suspend fun isEmailBlocked(email: String): Result<Boolean> {
        return Result.success(false)
    }
    
    /**
     * Check if a device is blocked - always returns false (no blocking after Firebase removal)
     */
    actual suspend fun isDeviceBlocked(deviceId: String): Result<Boolean> {
        return Result.success(false)
    }
    
    /**
     * Check device limit - always returns true (no limits after Firebase removal)
     */
    actual suspend fun checkDeviceLimit(email: String, deviceId: String): Result<Boolean> {
        return Result.success(true)
    }
}

actual fun createBlockingService(context: Any?): BlockingService? {
    return try {
        BlockingService(context as Context)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating BlockingService: ${e.message}", e)
        null
    }
}

