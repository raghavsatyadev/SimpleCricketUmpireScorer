package io.github.raghavsatyadev.support.compose.storage

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.android.EntryPointAccessors

class StorageInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    EntryPointAccessors.fromApplication(context, StorageEntryPoint::class.java).storageUtil()
  }

  override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}
