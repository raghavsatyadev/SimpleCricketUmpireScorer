package io.github.raghavsatyadev.library.support.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.raghavsatyadev.library.support.Constants
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
  single<RoomDatabase.Builder<AppDatabase>> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), Constants.DB.NAME)
    Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
  }
}
