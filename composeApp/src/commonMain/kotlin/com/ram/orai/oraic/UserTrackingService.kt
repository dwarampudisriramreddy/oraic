package com.ram.orai.oraic

/**
 * Service to track app usage for admin users
 */
expect class UserTrackingService {
    suspend fun trackAppUsage(userEmail: String, userName: String?, deviceId: String, deviceName: String)
    suspend fun getTrackedUsers(): Result<List<TrackedUser>>
}

data class TrackedUser(
    val email: String,
    val name: String?,
    val deviceId: String,
    val deviceName: String,
    val lastSeen: Long,
    val sessionCount: Int
)

expect fun createUserTrackingService(context: Any?): UserTrackingService?

