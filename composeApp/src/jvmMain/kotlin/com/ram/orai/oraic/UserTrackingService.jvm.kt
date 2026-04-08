package com.ram.orai.oraic

actual class UserTrackingService {
    actual suspend fun trackAppUsage(userEmail: String, userName: String?, deviceId: String, deviceName: String) {
        // No-op for desktop
    }
    
    actual suspend fun getTrackedUsers(): Result<List<TrackedUser>> {
        return Result.failure(Exception("User tracking not available on desktop"))
    }
}

actual fun createUserTrackingService(context: Any?): UserTrackingService? {
    return UserTrackingService()
}

