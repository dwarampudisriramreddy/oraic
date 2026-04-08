package com.ram.orai.oraic

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ram.orai.oraic.database.OraicDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        try {
            // Explicitly load SQLite JDBC driver to avoid DriverManager errors
            Class.forName("org.sqlite.JDBC")
        } catch (e: ClassNotFoundException) {
            System.err.println("Warning: SQLite JDBC driver not found. Database operations may fail.")
            e.printStackTrace()
        }
        
        val databasePath = File(System.getProperty("user.home"), ".oraic")
        databasePath.mkdirs()
        val dbFile = File(databasePath, "oraic.db")
        val dbExists = dbFile.exists()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        
        // Only create schema if database file is new
        if (!dbExists) {
            OraicDatabase.Schema.create(driver)
        }
        
        return driver
    }
}



