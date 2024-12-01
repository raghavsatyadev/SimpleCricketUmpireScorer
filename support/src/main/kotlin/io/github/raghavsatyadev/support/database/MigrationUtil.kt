package io.github.raghavsatyadev.support.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationUtil {

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
        }
    }
    val migrations: Array<Migration>
        get() = arrayOf(MIGRATION_1_2)
}