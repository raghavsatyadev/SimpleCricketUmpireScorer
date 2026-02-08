package io.github.raghavsatyadev.support.background

import androidx.work.ListenableWorker.Result
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.repository.FireStoreRepository
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

class MatchDataUploadUtil(
  private val fireStoreRepository: FireStoreRepository,
  private val recordDataUtil: MatchRecordDataUtil,
) {
  suspend fun updateAllMatchData(): Result {
    val matchRecords = recordDataUtil.getAll()

    try {
      val isSuccessful = fireStoreRepository.updateMatchRecords(matchRecords)
      return if (isSuccessful) {
        Result.success()
      } else {
        Result.failure()
      }
    } catch (e: Exception) {
      AppLog.loge(false, kotlinFileName, "updateAllMatchData", e, Exception())
      return Result.failure()
    }
  }

  suspend fun updateMatchData(matchRecordID: String): Result {
    val item = recordDataUtil.getItem(matchRecordID)

    try {
      val isSuccessful = fireStoreRepository.updateMatchRecord(item)
      return if (isSuccessful) {
        Result.success()
      } else {
        Result.failure()
      }
    } catch (e: Exception) {
      AppLog.loge(false, kotlinFileName, "updateMatchData", e, Exception())
      return Result.failure()
    }
  }
}
