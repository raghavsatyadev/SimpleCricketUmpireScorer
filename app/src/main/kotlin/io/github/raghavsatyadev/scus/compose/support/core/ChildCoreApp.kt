package io.github.raghavsatyadev.scus.compose.support.core

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import io.github.raghavsatyadev.support.compose.background.WorkSchedulerEntryPoint
import io.github.raghavsatyadev.support.core.CoreApp
import javax.inject.Inject

@HiltAndroidApp
class ChildCoreApp : CoreApp(), Configuration.Provider {
  override fun onCreate() {
    super.onCreate()
    setupWorker()
  }

  @Inject lateinit var workerFactory: HiltWorkerFactory

  override val workManagerConfiguration
      get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

  private fun setupWorker() {
    val scheduler =
      EntryPointAccessors.fromApplication(this, WorkSchedulerEntryPoint::class.java)
        .matchDataWorkScheduler()
    scheduler.enqueuePeriodic()
  }
}
