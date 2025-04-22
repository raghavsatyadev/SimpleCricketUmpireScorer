package io.github.raghavsatyadev.support.compose.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.raghavsatyadev.support.Constants.FieldKeys.MATCH_RECORD_ID
import kotlinx.coroutines.coroutineScope

@HiltWorker
class MatchDataUpdateWorker
@AssistedInject
constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val uploadUtil: MatchDataUploadUtil,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result = coroutineScope {
    inputData.getString(MATCH_RECORD_ID)?.let { matchRecordID ->
      uploadUtil.updateMatchData(matchRecordID)
    } ?: uploadUtil.updateAllMatchData()
  }
}
