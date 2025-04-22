package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.android.EntryPointAccessors
import io.github.raghavsatyadev.support.compose.google.FirebaseAppEntryPoint
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthEntryPoint
import io.github.raghavsatyadev.support.compose.google.FirestoreEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseAppInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    EntryPointAccessors.fromApplication(context, FirebaseAppEntryPoint::class.java).firebaseApp()
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}

class FirebaseAuthInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val entryPoint =
      EntryPointAccessors.fromApplication(context, FirebaseAuthEntryPoint::class.java)
    entryPoint.firebaseAuthUtil()
  }

  override fun dependencies() = listOf(FirebaseAppInitializer::class.java)
}

class FirestoreInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val entryPoint = EntryPointAccessors.fromApplication(context, FirestoreEntryPoint::class.java)
    CoroutineScope(Dispatchers.IO).launch { entryPoint.fireStoreUtil().initialize() }
  }

  override fun dependencies() =
    listOf(FirebaseAuthInitializer::class.java, RoomDBInitializer::class.java)
}
