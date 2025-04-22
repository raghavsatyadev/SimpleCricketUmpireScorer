package io.github.raghavsatyadev.support.compose.background

import androidx.work.ListenableWorker.Result
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordComposeDataUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchDataUploadUtil
@Inject
constructor(
  private val fireStoreUtil: FireStoreUtil,
  private val recordDataUtil: MatchRecordComposeDataUtil,
) {
  suspend fun updateAllMatchData(): Result {
    val matchRecords = recordDataUtil.getAll()

    try {
      val isSuccessful = fireStoreUtil.updateMatchRecords(matchRecords)
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
      val isSuccessful = fireStoreUtil.updateMatchRecord(item)
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
