package io.github.raghavsatyadev.support.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

@Database(
    version = Constants.DB.VERSION,
    entities = [MatchRecord::class],
    autoMigrations = [
    ]
)
@TypeConverters(value = [EssentialConverters::class, AppConverters::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDetailDao(): MatchRecordDataUtil.MatchRecordDao
}