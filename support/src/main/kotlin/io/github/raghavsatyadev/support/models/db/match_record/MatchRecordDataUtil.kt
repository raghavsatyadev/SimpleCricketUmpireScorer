package io.github.raghavsatyadev.support.models.db.match_record

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.database.BaseDao
import io.github.raghavsatyadev.support.database.BaseDataUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import kotlinx.coroutines.flow.Flow

class MatchRecordDataUtil : BaseDataUtil<MatchRecord, MatchRecordDataUtil.MatchRecordDao>() {
    companion object {
        @Volatile
        private var instance: MatchRecordDataUtil? = null

        @Synchronized
        fun getInstance(): MatchRecordDataUtil {
            if (instance == null) instance = MatchRecordDataUtil()
            return instance!!
        }
    }

    override fun getDao(): MatchRecordDao {
        return RoomDBUtil.getDatabase().songDetailDao()
    }

    override fun getTableName(): String {
        return Constants.DB.Tables.MATCH_RECORD_TABLE
    }

    override fun getPrimaryKey(): String {
        return Constants.DB.Tables.MATCH_RECORD_ID
    }

    fun getAllLive(sortKey: String = ""): Flow<List<MatchRecord>> {
        return getDao().getAllLive(SimpleSQLiteQuery(buildGetAllSortedQuery(sortKey)))
    }

    fun getCountLive(): Flow<Long> {
        return getDao().getCountLive(SimpleSQLiteQuery(buildGetCountQuery()))
    }

    fun getItemLive(id: Long): Flow<MatchRecord> {
        return getDao().getItemLive(SimpleSQLiteQuery(buildGetItemQuery(id.toString())))
    }

    @Dao
    abstract class MatchRecordDao : BaseDao<MatchRecord> {
        @RawQuery(observedEntities = [MatchRecord::class])
        abstract fun getAllLive(simpleSQLiteQuery: SimpleSQLiteQuery): Flow<List<MatchRecord>>

        @RawQuery(observedEntities = [MatchRecord::class])
        abstract fun getCountLive(supportSQLiteQuery: SupportSQLiteQuery): Flow<Long>

        @RawQuery(observedEntities = [MatchRecord::class])
        abstract fun getItemLive(query: SupportSQLiteQuery): Flow<MatchRecord>
    }
}