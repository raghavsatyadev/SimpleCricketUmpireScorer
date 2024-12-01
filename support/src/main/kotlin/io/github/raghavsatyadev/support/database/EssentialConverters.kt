package io.github.raghavsatyadev.support.database

import androidx.room.TypeConverter
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import java.util.Date

@Suppress("unused")
class EssentialConverters {
    // region Essential Converters
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millis: Long?): Date? {
        return millis?.let { Date(millis) }
    }

    @TypeConverter
    fun fromIntegerArrayList(data: ArrayList<Int>?): String {
        return data?.toJsonString() ?: "[]"
    }

    @TypeConverter
    fun toIntegerArrayList(s: String?): ArrayList<Int> {
        return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
    }

    @TypeConverter
    fun fromIntegerList(data: List<Int>?): String {
        return data?.toJsonString() ?: "[]"
    }

    @TypeConverter
    fun toIntegerList(s: String?): List<Int> {
        return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
    }

    @TypeConverter
    fun fromStringArrayList(data: ArrayList<String>?): String {
        return data?.toJsonString() ?: "[]"
    }

    @TypeConverter
    fun toStringArrayList(s: String?): ArrayList<String> {
        return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
    }

    @TypeConverter
    fun fromStringList(data: List<String>?): String {
        return data?.toJsonString() ?: "[]"
    }

    @TypeConverter
    fun toStringList(s: String?): List<String> {
        return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
    }
    // endregion
}