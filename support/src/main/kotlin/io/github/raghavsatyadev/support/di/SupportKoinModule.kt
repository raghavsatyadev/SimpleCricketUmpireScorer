package io.github.raghavsatyadev.support.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.WorkManager
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.MigrationUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
import io.github.raghavsatyadev.support.storage.StorageUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val supportModule = module {
  // Room
  single<AppDatabase> {
    Room.databaseBuilder(androidContext(), AppDatabase::class.java, Constants.DB.NAME)
      .addMigrations(*MigrationUtil.migrations)
      .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
      .fallbackToDestructiveMigration(true)
      .build()
  }
  singleOf(::RoomDBUtil)
  singleOf(::MatchRecordDataUtil)

  singleOf(::UiStateManager)

  // Networking
  singleOf(::StorageUtils)

  // WorkManager
  single { WorkManager.getInstance(androidContext()) }

  // Providers & Repositories
  single<io.github.raghavsatyadev.support.providers.StringResourceProvider> {
    io.github.raghavsatyadev.support.providers.AndroidStringResourceProvider(androidContext())
  }
}
