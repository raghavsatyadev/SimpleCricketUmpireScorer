package io.github.raghavsatyadev.library.support.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import io.github.raghavsatyadev.library.support.Constants
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord

@Database(entities = [MatchRecord::class], version = Constants.DB.VERSION)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun matchRecordDao(): MatchRecordDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
  override fun initialize(): AppDatabase
}
