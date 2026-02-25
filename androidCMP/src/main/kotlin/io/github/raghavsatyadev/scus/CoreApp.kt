package io.github.raghavsatyadev.scus

import android.app.Application
import io.github.raghavsatyadev.library.support.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory

class CoreApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin {
      androidLogger()
      androidContext(this@CoreApp)
      workManagerFactory()
    }
  }
}
