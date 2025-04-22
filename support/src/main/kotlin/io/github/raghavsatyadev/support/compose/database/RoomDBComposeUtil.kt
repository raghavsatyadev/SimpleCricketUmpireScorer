package io.github.raghavsatyadev.support.compose.database

import io.github.raghavsatyadev.support.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDBComposeUtil @Inject constructor(private val database: AppDatabase) {

  /** Returns the singleton AppDatabase instance. */
  fun getDatabase(): AppDatabase = database

  /** Clears all tables off the IO dispatcher. */
  fun deleteAll() {
    CoroutineScope(Dispatchers.IO).launch { database.clearAllTables() }
  }
}
