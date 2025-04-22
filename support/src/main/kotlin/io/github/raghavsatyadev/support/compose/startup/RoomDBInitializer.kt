package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import io.github.raghavsatyadev.support.database.RoomDBUtil

class RoomDbInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    RoomDBUtil.getDatabase(context)
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
