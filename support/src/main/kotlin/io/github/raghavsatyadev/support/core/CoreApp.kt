package io.github.raghavsatyadev.support.core

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CoreApp : Application(), CoroutineScope {

  private lateinit var job: Job

  private val handler = CoroutineExceptionHandler { _, exception ->
    AppLog.loge(false, kotlinFileName, "handler", exception, Exception())
  }

  override val coroutineContext: CoroutineContext
    get() {
      var context = Dispatchers.Main + job
      if (!BuildConfig.DEBUG) context += handler
      return context
    }

  companion object {
    @Volatile
    lateinit var instance: CoreApp
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
    job = Job()

    DynamicColors.applyToActivitiesIfAvailable(this)
  }

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
  }

  override fun onTerminate() {
    job.cancel()
    super.onTerminate()
  }
}
