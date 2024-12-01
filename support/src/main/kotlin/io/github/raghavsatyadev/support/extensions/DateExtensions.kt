package io.github.raghavsatyadev.support.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("unused")
object DateExtensions {
    // Format millis to date
    fun String.formatMillisToDate(
        dateFormat: String = "dd-MM-yyyy hh:mm a",
    ): String {
        val milliseconds = this.toLongOrNull() ?: return "Invalid date"
        val date = Date(milliseconds)
        val format = SimpleDateFormat(dateFormat, Locale.getDefault())
        return format.format(date)
    }

    fun String.formatDateToMillis(
        dateFormat: String = "dd-MM-yyyy hh:mm a",
    ): Long {
        val format = SimpleDateFormat(dateFormat, Locale.getDefault())
        val dateTime = format.parse(this)
        return dateTime?.time ?: 0
    }

    // Format millis to date
    fun Long.formatMillisToDate(
        dateFormat: String = "dd-MM-yyyy hh:mm a",
    ): String {
        val date = Date(this)
        val format = SimpleDateFormat(dateFormat, Locale.getDefault())
        return format.format(date)
    }
}