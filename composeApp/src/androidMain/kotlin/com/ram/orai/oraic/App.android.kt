package com.ram.orai.oraic

import android.content.Context

actual fun getDatabaseDriverFactory(): DatabaseDriverFactory {
    // This function should not be called directly in Android
    // DatabaseDriverFactory should be created with a Context parameter
    throw UnsupportedOperationException("Use DatabaseDriverFactory(context) constructor instead")
}

actual fun getAppContext(): Any? {
    return null // Will be provided via parameter in App composable
}

