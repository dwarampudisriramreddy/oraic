package com.ram.orai.oraic

import android.content.Context
import android.provider.Settings

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getDeviceId(context: Any?): String {
    val ctx = context as? Context ?: return "unknown"
    return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}

actual fun getDeviceName(context: Any?): String {
    val ctx = context as? Context ?: return "Unknown Device"
    return android.os.Build.MODEL ?: "Unknown Device"
}
