package io.github.raghavsatyadev.library.support.database

import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.raghavsatyadev.library.support.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
  single<RoomDatabase.Builder<AppDatabase>> {
    val context = androidContext()
    val dbFile = context.getDatabasePath(Constants.DB.NAME)
    Room.databaseBuilder<AppDatabase>(context = context, name = dbFile.absolutePath)
  }
}
