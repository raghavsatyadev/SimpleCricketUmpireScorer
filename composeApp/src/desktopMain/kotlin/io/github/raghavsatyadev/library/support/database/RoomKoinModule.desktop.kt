package io.github.raghavsatyadev.library.support.database

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.raghavsatyadev.library.support.Constants
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual fun platformDatabaseModule(): Module = module {
  single<RoomDatabase.Builder<AppDatabase>> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), Constants.DB.NAME)
    Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
  }
}
