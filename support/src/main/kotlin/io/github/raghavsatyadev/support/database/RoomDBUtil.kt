package io.github.raghavsatyadev.support.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomDBUtil(private val database: AppDatabase) {

  /** Clears all tables off the IO dispatcher. */
  fun deleteAll() {
    CoroutineScope(Dispatchers.IO).launch { database.clearAllTables() }
  }
}
