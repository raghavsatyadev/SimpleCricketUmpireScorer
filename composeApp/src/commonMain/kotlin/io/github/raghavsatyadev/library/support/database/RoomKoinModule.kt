package io.github.raghavsatyadev.library.support.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformDatabaseModule(): Module

val commonDatabaseModule = module {
  single {
    get<RoomDatabase.Builder<AppDatabase>>()
      .fallbackToDestructiveMigration(true)
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .build()
  }
  single { get<AppDatabase>().matchRecordDao() }
  single<MatchRecordRepository> { MatchRecordRepositoryImpl(get()) }
}
