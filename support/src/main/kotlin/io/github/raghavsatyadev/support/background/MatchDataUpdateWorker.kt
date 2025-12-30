package io.github.raghavsatyadev.support.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.raghavsatyadev.support.Constants.FieldKeys.MATCH_RECORD_ID
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MatchDataUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams), KoinComponent {

  private val uploadUtil: MatchDataUploadUtil by inject()

  override suspend fun doWork(): Result = coroutineScope {
    inputData.getString(MATCH_RECORD_ID)?.let { matchRecordID ->
      uploadUtil.updateMatchData(matchRecordID)
    } ?: uploadUtil.updateAllMatchData()
  }
}
