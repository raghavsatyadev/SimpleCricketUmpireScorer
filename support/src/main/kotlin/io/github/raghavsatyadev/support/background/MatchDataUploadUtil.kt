package io.github.raghavsatyadev.support.background

import androidx.work.ListenableWorker.Result
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

object MatchDataUploadUtil {
    fun updateAllMatchData(): Result {
        MatchRecordDataUtil
            .getInstance()
            .getAll()
        return Result.success()
    }

    fun updateMatchData(matchRecordID: String): Result {
        MatchRecordDataUtil
            .getInstance()
            .getItem(matchRecordID)
        return Result.success()
    }
}