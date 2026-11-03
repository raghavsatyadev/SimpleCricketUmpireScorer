package io.github.raghavsatyadev.library.support.database

import androidx.room3.ColumnTypeConverter
import io.github.raghavsatyadev.library.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.library.support.extensions.serializer.SerializationExtensions.toKotlinObject

@Suppress("unused")
class EssentialConverters {
  // region Essential Converters

  @ColumnTypeConverter
  fun fromIntegerArrayList(data: ArrayList<Int>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toIntegerArrayList(s: String?): ArrayList<Int> {
    return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromIntegerList(data: List<Int>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toIntegerList(s: String?): List<Int> {
    return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromStringArrayList(data: ArrayList<String>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toStringArrayList(s: String?): ArrayList<String> {
    return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromStringList(data: List<String>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toStringList(s: String?): List<String> {
    return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
  }

  // endregion

  // region App Specific Converters

  // endregion
}
