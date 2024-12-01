package io.github.raghavsatyadev.support

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.MigrationUtil
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MigrationTest {
    private val testDB = "migration-test"

    // Array of all migrations
    private val allMigrations = MigrationUtil.migrations

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(testDB, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            testDB
        ).addMigrations(*allMigrations).build().apply {
            openHelper.writableDatabase.close()
        }

        println("Successfully migrated all")
    }
}