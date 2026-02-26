package io.github.raghavsatyadev.library.support.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class RoomConverters {
  @TypeConverter
  fun fromStringList(data: List<String>?): String {
    return data?.let { Json.encodeToString(it) } ?: "[]"
  }

  @TypeConverter
  fun toStringList(s: String?): List<String> {
    return s?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
  }
}
