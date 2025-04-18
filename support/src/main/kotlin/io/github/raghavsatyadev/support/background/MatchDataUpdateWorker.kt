package io.github.raghavsatyadev.support.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.raghavsatyadev.support.core.CoreApp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

class MatchDataUpdateWorker(appContext: Context, workerParams: WorkerParameters) :
  CoroutineWorker(appContext, workerParams) {

  companion object {
    private const val UNIQUE_WORK_NAME = "MatchDataUpdate"
    private const val UNIQUE_WORK_NAME_PERIODIC = "MatchDataUpdatePeriodic"
    private const val MATCH_RECORD_ID = "matchRecordID"

    private val manager by lazy { WorkManager.Companion.getInstance(CoreApp.instance) }
    private val oneTimeWorkStatus by lazy {
      manager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
    }
    private val periodicWorkStatus by lazy {
      manager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME_PERIODIC)
    }

    fun updateMatchDataPeriodically() {
      val periodicWorkRequest =
        PeriodicWorkRequestBuilder<MatchDataUpdateWorker>(1, TimeUnit.DAYS)
          .setInputData(Data.Builder().build())
          .build()

      manager.enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME_PERIODIC,
        ExistingPeriodicWorkPolicy.KEEP,
        periodicWorkRequest,
      )
    }

    fun updateMatchData(matchRecordID: String) {
      val oneTimeWorkRequest =
        OneTimeWorkRequestBuilder<MatchDataUpdateWorker>()
          .setInputData(Data.Builder().putString(MATCH_RECORD_ID, matchRecordID).build())
          .build()

      manager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, oneTimeWorkRequest)
    }

    suspend fun listenToWorkStatus(
      onOneTimeWorkStatusChanged: ((Boolean) -> Unit),
      onPeriodicWorkStatusChanged: ((Boolean) -> Unit),
    ) {
      oneTimeWorkStatus.collectLatest { infos ->
        val oneTimeWorkRunning = infos.any { it.state == WorkInfo.State.RUNNING }

        onOneTimeWorkStatusChanged(oneTimeWorkRunning)
      }
      periodicWorkStatus.collectLatest { infos ->
        val periodicWorkRunning = infos.any { it.state == WorkInfo.State.RUNNING }
        onPeriodicWorkStatusChanged(periodicWorkRunning)
      }
    }
  }

  override suspend fun doWork(): Result = coroutineScope {
    inputData.getString(MATCH_RECORD_ID)?.let { matchRecordID ->
      MatchDataUploadUtil.updateMatchData(matchRecordID)
    } ?: MatchDataUploadUtil.updateAllMatchData()
  }
}
