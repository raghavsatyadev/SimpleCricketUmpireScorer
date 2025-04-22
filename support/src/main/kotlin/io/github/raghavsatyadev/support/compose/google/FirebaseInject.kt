package io.github.raghavsatyadev.support.compose.google

import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

  @Provides
  @Singleton
  fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
    // Return existing or initialize new
    return FirebaseApp.initializeApp(context)!!
  }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseAppEntryPoint {
  fun firebaseApp(): FirebaseApp
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseAuthEntryPoint {
  fun firebaseAuthUtil(): FirebaseAuthUtil
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirestoreEntryPoint {
  fun fireStoreUtil(): FireStoreUtil
}
