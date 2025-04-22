package io.github.raghavsatyadev.support.compose.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.MigrationUtil
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, Constants.DB.NAME)
      .addMigrations(*MigrationUtil.migrations)
      .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
      .fallbackToDestructiveMigration(true)
      .build()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RoomDbEntryPoint {
  fun roomDBUtil(): RoomDBComposeUtil
}
