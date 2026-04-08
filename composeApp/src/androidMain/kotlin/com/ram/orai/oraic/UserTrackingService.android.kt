package com.ram.orai.oraic

import android.content.Context
import android.util.Log

private const val TAG = "UserTrackingService"

actual class UserTrackingService(private val context: Context) {
    /**
     * Track app usage - no-op after Firebase removal
     */
    actual suspend fun trackAppUsage(userEmail: String, userName: String?, deviceId: String, deviceName: String) {
        // No-op: tracking disabled after Firebase removal
    }
    
    /**
     * Get all tracked users - returns empty list after Firebase removal
     */
    actual suspend fun getTrackedUsers(): Result<List<TrackedUser>> {
        return Result.success(emptyList())
    }
}

actual fun createUserTrackingService(context: Any?): UserTrackingService? {
    return try {
        UserTrackingService(context as Context)
    } catch (e: Exception) {
        Log.e(TAG, "Error creating UserTrackingService: ${e.message}", e)
        null
    }
}

