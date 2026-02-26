package io.github.raghavsatyadev.library.support.repository

import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import kotlinx.coroutines.flow.Flow

interface MatchRecordRepository {
  fun getItemLive(id: String): Flow<MatchRecord>

  suspend fun getItem(id: String): MatchRecord

  suspend fun update(t: MatchRecord): Int

  fun getAllLive(sortKey: String = ""): Flow<List<MatchRecord>>

  fun getCountLive(): Flow<Long>

  suspend fun delete(primaryKeyId: String): Int

  suspend fun upsert(allNewRecords: List<MatchRecord>)

  suspend fun updateServerTime(id: String, time: Long)
}
