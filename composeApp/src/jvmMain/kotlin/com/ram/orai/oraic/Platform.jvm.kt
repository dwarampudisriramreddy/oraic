package com.ram.orai.oraic

class DesktopPlatform : Platform {
    override val name: String = System.getProperty("os.name") ?: "Desktop"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual fun getDeviceId(context: Any?): String {
    return java.net.InetAddress.getLocalHost().hostName ?: "unknown"
}

actual fun getDeviceName(context: Any?): String {
    return System.getProperty("os.name") ?: "Unknown Device"
}
