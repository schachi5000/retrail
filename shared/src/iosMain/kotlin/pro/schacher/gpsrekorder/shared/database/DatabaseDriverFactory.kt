package pro.schacher.gpsrekorder.shared.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import database.AppDatabase
import pro.schacher.gpsrekorder.shared.database.DATABASE_NAME

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(AppDatabase.Schema, DATABASE_NAME)
}