package io.github.raghavsatyadev.support.compose.background

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

  @Provides
  @Singleton
  fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
    return WorkManager.getInstance(context)
  }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkSchedulerEntryPoint {
  fun matchDataWorkScheduler(): MatchDataWorkScheduler
}
