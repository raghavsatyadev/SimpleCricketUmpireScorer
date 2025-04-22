package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject

class WorkManagerInitializer : Initializer<WorkManager>, Configuration.Provider {
  @Inject lateinit var workerFactory: HiltWorkerFactory

  override fun create(context: Context): WorkManager {
    InitializerEntryPoint.resolve(context).inject(this)
    WorkManager.initialize(context, workManagerConfiguration)
    return WorkManager.getInstance(context)
  }

  override val workManagerConfiguration: Configuration
    get() {
      return Configuration.Builder().setWorkerFactory(workerFactory).build()
    }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(
      DependencyGraphInitializer::class.java,
      RoomDBInitializer::class.java,
      FirestoreInitializer::class.java,
    )
}
