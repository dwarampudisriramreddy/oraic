package com.ram.orai.oraic

actual fun getDatabaseDriverFactory(): DatabaseDriverFactory {
    return DatabaseDriverFactory()
}

actual fun getAppContext(): Any? {
    return null // iOS context can be added later if needed
}
