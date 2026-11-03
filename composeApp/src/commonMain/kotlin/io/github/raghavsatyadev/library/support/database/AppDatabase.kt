package io.github.raghavsatyadev.library.support.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import io.github.raghavsatyadev.library.support.Constants
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord

@Database(entities = [MatchRecord::class], version = Constants.DB.VERSION)
@ConstructedBy(AppDatabaseConstructor::class)
@ColumnTypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun matchRecordDao(): MatchRecordDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase
}
