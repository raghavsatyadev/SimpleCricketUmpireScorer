package io.github.raghavsatyadev.support.compose.startup

import android.content.Context
import androidx.startup.Initializer
import dagger.hilt.android.EntryPointAccessors
import io.github.raghavsatyadev.support.compose.database.RoomDbEntryPoint

class RoomDBInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val roomDbUtil =
      EntryPointAccessors.fromApplication(context, RoomDbEntryPoint::class.java).roomDBUtil()

    roomDbUtil.getDatabase()
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
