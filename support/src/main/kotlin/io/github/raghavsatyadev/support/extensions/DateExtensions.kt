package io.github.raghavsatyadev.support.extensions

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateExtensions {
  private const val DEFAULT_DATE_FORMAT = "dd-MM-yyyy hh:mm a"

  // Format millis to date
  fun String.formatMillisToDate(dateFormat: String = DEFAULT_DATE_FORMAT): String {
    val milliseconds = this.toLongOrNull() ?: return "Invalid date"
    val date = Date(milliseconds)
    val format = SimpleDateFormat(dateFormat, Locale.getDefault())
    return format.format(date)
  }

  fun String.formatDateToMillis(dateFormat: String = DEFAULT_DATE_FORMAT): Long {
    val format = SimpleDateFormat(dateFormat, Locale.getDefault())
    val dateTime = format.parse(this)
    return dateTime?.time ?: 0
  }

  // Format millis to date
  fun Long.formatMillisToDate(dateFormat: String = DEFAULT_DATE_FORMAT): String {
    val date = Date(this)
    val format = SimpleDateFormat(dateFormat, Locale.getDefault())
    return format.format(date)
  }

  fun LocalDateTime.toZoneEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

  fun Long.toZoneLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

  fun Long.toZoneLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

  fun LocalDateTime.formatToDateString(dateFormat: String = DEFAULT_DATE_FORMAT): String {
    val format = DateTimeFormatter.ofPattern(dateFormat, Locale.getDefault())
    return this.format(format)
  }
}
