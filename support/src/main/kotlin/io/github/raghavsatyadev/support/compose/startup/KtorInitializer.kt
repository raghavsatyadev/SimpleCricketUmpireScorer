package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.android.EntryPointAccessors
import io.github.raghavsatyadev.support.compose.networking.KtorEntryPoint
import io.github.raghavsatyadev.support.compose.storage.StorageInitializer

class KtorInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    EntryPointAccessors.fromApplication(context, KtorEntryPoint::class.java).httpClient()
  }

  override fun dependencies() = listOf(StorageInitializer::class.java)
}
