package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.FirebaseApp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseAppEntryPoint {
  fun firebaseApp(): FirebaseApp
}

class FirebaseAppInitializer : Initializer<Unit> {
  override fun create(context: Context) {
      EntryPointAccessors.fromApplication(context, FirebaseAppEntryPoint::class.java).firebaseApp()
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseAuthEntryPoint {
  fun firebaseAuthUtil(): FirebaseAuthUtil
}

class FirebaseAuthInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val entryPoint =
      EntryPointAccessors.fromApplication(context, FirebaseAuthEntryPoint::class.java)
    entryPoint.firebaseAuthUtil()
  }

  override fun dependencies() = listOf(FirebaseAppInitializer::class.java)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirestoreEntryPoint {
  fun fireStoreUtil(): FireStoreUtil
}

class FirestoreInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val entryPoint = EntryPointAccessors.fromApplication(context, FirestoreEntryPoint::class.java)
    CoroutineScope(Dispatchers.IO).launch { entryPoint.fireStoreUtil().initialize() }
  }

  override fun dependencies() = listOf(FirebaseAuthInitializer::class.java)
}
