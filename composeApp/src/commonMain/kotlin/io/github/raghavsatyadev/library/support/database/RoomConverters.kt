package io.github.raghavsatyadev.library.support.database

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json

class RoomConverters {
  @ColumnTypeConverter
  fun fromStringList(data: List<String>?): String {
    return data?.let { Json.encodeToString(it) } ?: "[]"
  }

  @ColumnTypeConverter
  fun toStringList(s: String?): List<String> {
    return s?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
  }
}
