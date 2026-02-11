package io.github.raghavsatyadev.library.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

object MigrationUtil {

  private val MIGRATION_1_2: Migration =
    object : Migration(1, 2) {
      override fun migrate(connection: SQLiteConnection) {}
    }
  val migrations: Array<Migration>
    get() = arrayOf(MIGRATION_1_2)
}
