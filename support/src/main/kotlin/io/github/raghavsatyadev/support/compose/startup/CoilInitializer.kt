package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import okio.Path.Companion.toOkioPath

class CoilInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    // exactly your CoreApp.setupCoil() logic, but using context instead of CoreApp.instance
    SingletonImageLoader.setSafe {
      ImageLoader.Builder(context.applicationContext)
        .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.25).build() }
        .diskCache {
          DiskCache.Builder()
            .directory(context.cacheDir.resolve("image_cache").toOkioPath())
            .maxSizeBytes(5L * 1024 * 1024)
            .build()
        }
        .logger(DebugLogger())
        .build()
    }
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
