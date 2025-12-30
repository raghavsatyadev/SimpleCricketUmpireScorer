package io.github.raghavsatyadev.support.background

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class MatchDataWorkScheduler(private val workManager: WorkManager) {
  private companion object {
    const val UNIQUE_WORK_NAME = "MatchDataUpdate"
    const val UNIQUE_WORK_NAME_PERIODIC = "MatchDataUpdatePeriodic"
    const val MATCH_RECORD_ID = "matchRecordID"
  }

  /** Enqueue a one‑off update for a specific matchRecordID */
  fun enqueueOneTime(matchRecordID: String) {
    val request =
      OneTimeWorkRequestBuilder<MatchDataUpdateWorker>()
        .setInputData(workDataOf(MATCH_RECORD_ID to matchRecordID))
        .build()

    workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
  }

  /** Enqueue your daily periodic update (every 24h) */
  fun enqueuePeriodic() {
    val request = PeriodicWorkRequestBuilder<MatchDataUpdateWorker>(1, TimeUnit.DAYS).build()

    workManager.enqueueUniquePeriodicWork(
      UNIQUE_WORK_NAME_PERIODIC,
      ExistingPeriodicWorkPolicy.KEEP,
      request,
    )
  }

  /** Flow that emits `true` when the one‑off work is running */
  fun oneTimeWorkRunning(): Flow<Boolean> =
    workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME).map { infos ->
      infos.any { it.state == WorkInfo.State.RUNNING }
    }

  /** Flow that emits `true` when the periodic work is running */
  fun periodicWorkRunning(): Flow<Boolean> =
    workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME_PERIODIC).map { infos ->
      infos.any { it.state == WorkInfo.State.RUNNING }
    }
}
