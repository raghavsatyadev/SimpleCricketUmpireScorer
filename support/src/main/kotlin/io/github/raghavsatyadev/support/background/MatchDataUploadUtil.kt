package io.github.raghavsatyadev.support.background

import androidx.work.ListenableWorker.Result
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

object MatchDataUploadUtil {
    suspend fun updateAllMatchData(): Result {
        val matchRecords = MatchRecordDataUtil
            .getInstance()
            .getAll()

        try {
            val isSuccessful = FireStoreUtil
                .getInstance()
                .updateMatchRecords(matchRecords)
            return if (isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            AppLog.loge(
                false,
                kotlinFileName,
                "updateAllMatchData",
                e,
                Exception()
            )
            return Result.failure()
        }
    }

    suspend fun updateMatchData(matchRecordID: String): Result {
        val item = MatchRecordDataUtil
            .getInstance()
            .getItem(matchRecordID)

        try {
            val isSuccessful = FireStoreUtil
                .getInstance()
                .updateMatchRecord(item)
            return if (isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            AppLog.loge(
                false,
                kotlinFileName,
                "updateMatchData",
                e,
                Exception()
            )
            return Result.failure()
        }
    }
}