package io.github.raghavsatyadev.support.models.db.match_record

import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.BaseDataUtil
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRecordComposeDataUtil @Inject constructor(private val database: AppDatabase) :
  BaseDataUtil<MatchRecord, MatchRecordDataUtil.MatchRecordDao>() {

  override fun getDao(): MatchRecordDataUtil.MatchRecordDao = database.songDetailDao()

  override fun getTableName(): String = Constants.DB.Tables.MATCH_RECORD_TABLE

  override fun getPrimaryKey(): String = Constants.FieldKeys.MATCH_RECORD_ID

  fun getAllLive(sortKey: String = ""): Flow<List<MatchRecord>> =
    database.songDetailDao().getAllLive(SimpleSQLiteQuery(buildGetAllSortedQuery(sortKey)))

  fun getCountLive(): Flow<Long> =
    database.songDetailDao().getCountLive(SimpleSQLiteQuery(buildGetCountQuery()))

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
    database.songDetailDao().updateServerTime(id, time.time)

  fun getItemLive(id: String): Flow<MatchRecord> =
    database.songDetailDao().getItemLive(SimpleSQLiteQuery(buildGetItemQuery(id)))
}
