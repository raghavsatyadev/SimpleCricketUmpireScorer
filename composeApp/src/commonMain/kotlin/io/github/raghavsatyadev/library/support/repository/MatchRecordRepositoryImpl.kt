package io.github.raghavsatyadev.library.support.repository

import io.github.raghavsatyadev.library.support.database.MatchRecordDao
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class MatchRecordRepositoryImpl(private val matchRecordDao: MatchRecordDao) :
  MatchRecordRepository {
  override fun getItemLive(id: String): Flow<MatchRecord> = matchRecordDao.getItemLive(id)

  override suspend fun getItem(id: String): MatchRecord = matchRecordDao.getItem(id)

  override suspend fun update(t: MatchRecord): Int {
    t.localUpdateDateTime = Clock.System.now().toEpochMilliseconds()
    return matchRecordDao.update(t)
  }

  override fun getAllLive(sortKey: String): Flow<List<MatchRecord>> = matchRecordDao.getAllLive()

  override fun getCountLive(): Flow<Long> = matchRecordDao.getCountLive()

  override suspend fun delete(primaryKeyId: String): Int = matchRecordDao.delete(primaryKeyId)

  override suspend fun upsert(allNewRecords: List<MatchRecord>) {
    val now = Clock.System.now().toEpochMilliseconds()
    val allOld = matchRecordDao.getAll()
    val (present, fresh) =
      allNewRecords.partition { new ->
        new.localUpdateDateTime = now
        allOld
          .find { it.matchRecordId == new.matchRecordId }
          ?.let { (new.serverUpdateDateTime ?: 0L) > (it.localUpdateDateTime ?: 0L) } == true
      }
    matchRecordDao.insertIgnore(fresh)
    matchRecordDao.update(present)
  }

  override suspend fun updateServerTime(id: String, time: Long) {
    matchRecordDao.updateServerTime(id, time)
  }
}
