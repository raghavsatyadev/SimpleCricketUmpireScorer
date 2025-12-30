@file:OptIn(KoinExperimentalAPI::class)

package io.github.raghavsatyadev.scus.support.core

import android.widget.Toast
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import com.google.android.gms.ads.MobileAds
import io.github.raghavsatyadev.scus.support.di.appModule
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.background.MatchDataWorkScheduler
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin

class ChildCoreApp : CoreApp() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@ChildCoreApp)
      workManagerFactory()
      modules(appModule)
    }

    setupWorker()
    setupCoil()
    setupAds()
  }

  private val scheduler: MatchDataWorkScheduler by inject()

  private fun setupWorker() {
    scheduler.enqueuePeriodic()
  }

  private fun setupCoil() {
    SingletonImageLoader.setSafe {
      ImageLoader.Builder(this)
        .memoryCache { MemoryCache.Builder().maxSizePercent(this, 0.25).build() }
        .diskCache {
          DiskCache.Builder()
            .directory(cacheDir.resolve("image_cache").toOkioPath())
            .maxSizeBytes(5L * 1024 * 1024)
            .build()
        }
        .logger(DebugLogger())
        .build()
    }
  }

  private fun setupAds() {
    if (checkPlayServiceAvailability()) {
      if (BuildConfig.DEBUG) {
        MobileAds.setRequestConfiguration(
          MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTestDeviceIds(listOf("2B39C5E2140FC15DD45814184D9B515E"))
            .build()
        )
      }
      MobileAds.initialize(this)
    } else {
      Toast.makeText(this, R.string.warning_update_play_service, Toast.LENGTH_SHORT).show()
    }
  }
}
