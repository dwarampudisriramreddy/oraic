package com.ram.orai.oraic

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ram.orai.oraic.database.OraicDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = OraicDatabase.Schema,
            context = context,
            name = "oraic.db"
        )
    }
}

