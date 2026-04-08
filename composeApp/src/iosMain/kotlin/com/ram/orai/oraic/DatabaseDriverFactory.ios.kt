package com.ram.orai.oraic

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ram.orai.oraic.database.OraicDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = OraicDatabase.Schema,
            name = "oraic.db"
        )
    }
}




