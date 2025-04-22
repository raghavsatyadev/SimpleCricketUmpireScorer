package io.github.raghavsatyadev.support.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.core.CoreApp
import kotlinx.coroutines.launch

object RoomDBUtil {
  @Volatile private var database: AppDatabase? = null

  @Synchronized
  fun getDatabase(context: Context): AppDatabase {
    return database
      ?: synchronized(this) {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, Constants.DB.NAME)
          .addMigrations(*MigrationUtil.migrations)
          .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
          .fallbackToDestructiveMigration(true)
          .build()
          .also { database = it }
      }
  }

  @Synchronized
  fun getInstance(): AppDatabase {
    return database
      ?: throw IllegalStateException("Database not initialized. Call getDatabase(context) first.")
  }

  fun deleteAll() {
    CoreApp.instance.launch { getInstance().clearAllTables() }
  }
}
