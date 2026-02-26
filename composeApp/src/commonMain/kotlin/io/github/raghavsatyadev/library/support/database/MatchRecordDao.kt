package io.github.raghavsatyadev.library.support.database

import androidx.room.Dao
import androidx.room.Query
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao : BaseDao<MatchRecord> {
  @Query("SELECT * FROM match_record") fun getAllLive(): Flow<List<MatchRecord>>

  @Query("SELECT COUNT(match_record_id) FROM match_record") fun getCountLive(): Flow<Long>

  @Query("SELECT * FROM match_record WHERE match_record_id = :id LIMIT 1")
  fun getItemLive(id: String): Flow<MatchRecord>

  @Query("SELECT * FROM match_record WHERE match_record_id = :id LIMIT 1")
  suspend fun getItem(id: String): MatchRecord

  @Query("DELETE FROM match_record WHERE match_record_id = :id") suspend fun delete(id: String): Int

  @Query("SELECT * FROM match_record") suspend fun getAll(): List<MatchRecord>

  @Query("UPDATE match_record SET server_update_date_time = :time WHERE match_record_id = :id")
  suspend fun updateServerTime(id: String, time: Long)
}
