package io.github.raghavsatyadev.support.core

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.multidex.MultiDex
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.util.DebugLogger
import com.google.android.gms.ads.MobileAds
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.background.MatchDataUpdateWorker
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
import io.github.raghavsatyadev.support.networking.KtorUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CoreApp : Application(), CoroutineScope {

    private lateinit var job: Job

    private val handler = CoroutineExceptionHandler { _, exception ->
        AppLog.loge(
            false,
            kotlinFileName,
            "handler",
            exception,
            Exception()
        )
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
        // setupGoogleServices()
        RoomDBUtil.getDatabase()
        KtorUtil.httpClient
        setupCoil()

        setupWorker()
    }

    private fun setupWorker() {
        MatchDataUpdateWorker.updateMatchDataPeriodically()
    }

    private fun setupCoil() {
        SingletonImageLoader.setSafe {
            ImageLoader
                .Builder(instance)
                .memoryCache {
                    MemoryCache
                        .Builder()
                        .maxSizePercent(
                            instance,
                            0.25
                        )
                        .build()
                }
                .diskCache {
                    DiskCache
                        .Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(5 * 1024 * 1024)
                        .build()
                }
                .logger(DebugLogger())
                .build()
        }
    }

    private fun setupGoogleServices() {
        if (checkPlayServiceAvailability()) {
            MobileAds.initialize(this)
            val firebaseApp = FirebaseApp.initializeApp(this)
            firebaseApp?.let {
                FirebaseAuthUtil.create(it)
                FireStoreUtil.create(it)
            }
        } else {
            Toast
                .makeText(
                    this,
                    R.string.warning_update_play_service,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onTerminate() {
        job.cancel()
        super.onTerminate()
    }
}