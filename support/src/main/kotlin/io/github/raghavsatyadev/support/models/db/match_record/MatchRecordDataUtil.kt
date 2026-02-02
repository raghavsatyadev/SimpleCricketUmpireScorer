package io.github.raghavsatyadev.support.models.db.match_record

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.raghavsatyadev.support.Constants.DB.Tables
import io.github.raghavsatyadev.support.Constants.FieldKeys
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.BaseDao
import io.github.raghavsatyadev.support.database.BaseDataUtil
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MatchRecordDataUtil(private val database: AppDatabase) :
  BaseDataUtil<MatchRecord, MatchRecordDataUtil.MatchRecordDao>() {

  override fun getDao(): MatchRecordDao = database.matchRecordDao()

  override fun getTableName(): String = Tables.MATCH_RECORD_TABLE

  override fun getPrimaryKey(): String = FieldKeys.MATCH_RECORD_ID

  fun getAllLive(sortKey: String = ""): Flow<List<MatchRecord>> =
    database.matchRecordDao().getAllLive(SimpleSQLiteQuery(buildGetAllSortedQuery(sortKey)))

  fun getCountLive(): Flow<Long> =
    database.matchRecordDao().getCountLive(SimpleSQLiteQuery(buildGetCountQuery()))

  override fun delete(primaryKeyId: String): Int {
    return database.matchRecordDao().delete(primaryKeyId)
  }

  override fun update(t: MatchRecord): Int {
    t.localUpdateDateTime = Date()
    return super.update(t)
  }

  fun upsert(allNewRecords: List<MatchRecord>) {
    val now = Date()
    val allOld = getAll()
    val (present, fresh) =
      allNewRecords.partition { new ->
        new.localUpdateDateTime = now
        allOld
          .find { it.matchRecordId == new.matchRecordId }
          ?.let { new.serverUpdateDateTime!! > it.localUpdateDateTime!! } == true
      }
    insertIgnore(fresh)
    update(present)
  }

  fun updateServerTime(id: String, time: Date) =
    database.matchRecordDao().updateServerTime(id, time.time)

  fun getItemLive(id: String): Flow<MatchRecord> =
    database.matchRecordDao().getItemLive(SimpleSQLiteQuery(buildGetItemQuery(id)))

  @Dao
  interface MatchRecordDao : BaseDao<MatchRecord> {
    @RawQuery(observedEntities = [MatchRecord::class])
    fun getAllLive(simpleSQLiteQuery: SimpleSQLiteQuery): Flow<List<MatchRecord>>

    @Query("DELETE FROM ${Tables.MATCH_RECORD_TABLE} WHERE ${FieldKeys.MATCH_RECORD_ID} = :id")
    fun delete(id: String): Int

    @RawQuery(observedEntities = [MatchRecord::class])
    fun getCountLive(supportSQLiteQuery: SupportSQLiteQuery): Flow<Long>

    @RawQuery(observedEntities = [MatchRecord::class])
    fun getItemLive(query: SupportSQLiteQuery): Flow<MatchRecord>

    @Query(
      "UPDATE ${Tables.MATCH_RECORD_TABLE} SET ${FieldKeys.SERVER_UPDATE_DATE_TIME} = :time WHERE ${FieldKeys.MATCH_RECORD_ID} = :id"
    )
    fun updateServerTime(id: String, time: Long)
  }
}
