package io.github.raghavsatyadev.support.models.db.match_record

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.raghavsatyadev.support.Constants.DB.Tables
import io.github.raghavsatyadev.support.Constants.FieldKeys
import io.github.raghavsatyadev.support.database.BaseDao
import io.github.raghavsatyadev.support.database.BaseDataUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import kotlinx.coroutines.flow.Flow
import java.util.Date

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
        return RoomDBUtil
            .getDatabase()
            .songDetailDao()
    }

    override fun getTableName(): String {
        return Tables.MATCH_RECORD_TABLE
    }

    override fun getPrimaryKey(): String {
        return FieldKeys.MATCH_RECORD_ID
    }

    fun getAllLive(sortKey: String = ""): Flow<List<MatchRecord>> {
        return getDao().getAllLive(SimpleSQLiteQuery(buildGetAllSortedQuery(sortKey)))
    }

    fun getCountLive(): Flow<Long> {
        return getDao().getCountLive(SimpleSQLiteQuery(buildGetCountQuery()))
    }

    override fun update(t: MatchRecord): Int {
        t.localUpdateDateTime = Date()
        return super.update(t)
    }

    fun upsert(allNewRecords: List<MatchRecord>) {
        val now = Date()

        val allOldRecords = getAll()

        val (presentRecords, newRecords) = allNewRecords.partition { newRecord ->
            newRecord.localUpdateDateTime = now
            val foundRecord = allOldRecords.find {
                newRecord.matchRecordId == it.matchRecordId
            }
            foundRecord?.let {
                newRecord.serverUpdateDateTime!! > foundRecord.localUpdateDateTime!!
            } == true
        }

        insertIgnore(newRecords)
        update(presentRecords)
    }

    fun updateServerTime(
        id: String,
        time: Date,
    ) {
        return getDao().updateServerTime(
            id,
            time.time,
        )
    }

    fun getItemLive(id: String): Flow<MatchRecord> {
        return getDao().getItemLive(SimpleSQLiteQuery(buildGetItemQuery(id)))
    }

    @Dao
    interface MatchRecordDao : BaseDao<MatchRecord> {
        @RawQuery(observedEntities = [MatchRecord::class])
        fun getAllLive(simpleSQLiteQuery: SimpleSQLiteQuery): Flow<List<MatchRecord>>

        @RawQuery(observedEntities = [MatchRecord::class])
        fun getCountLive(supportSQLiteQuery: SupportSQLiteQuery): Flow<Long>

        @RawQuery(observedEntities = [MatchRecord::class])
        fun getItemLive(query: SupportSQLiteQuery): Flow<MatchRecord>

        @Query("UPDATE ${Tables.MATCH_RECORD_TABLE} SET ${FieldKeys.SERVER_UPDATE_DATE_TIME} = :time WHERE ${FieldKeys.MATCH_RECORD_ID} = :id")
        fun updateServerTime(
            id: String,
            time: Long,
        )
    }
}