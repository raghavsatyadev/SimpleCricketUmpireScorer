package io.github.raghavsatyadev.library.extensions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateExtensions {

  private val userDisplayFormat =
    LocalDateTime.Format {
      day()
      char('-')
      monthNumber()
      char('-')
      year()
      char(' ')
      amPmHour()
      char(':')
      minute()
      char(' ')
      amPmMarker("AM", "PM")
    }

  fun String.formatMillisToDate(
    dateFormat: DateTimeFormat<LocalDateTime> = userDisplayFormat
  ): String {
    val milliseconds = this.toLongOrNull() ?: return "Invalid date"
    return milliseconds.formatMillisToDate(dateFormat)
  }

  fun Long.formatMillisToDate(
    dateFormat: DateTimeFormat<LocalDateTime> = userDisplayFormat
  ): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return datetime.format(dateFormat)
  }

  fun String.formatDateToMillis(
    dateFormat: DateTimeFormat<LocalDateTime> = userDisplayFormat
  ): Long {
    return try {
      val datetime = dateFormat.parse(this)
      datetime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) {
      0L
    }
  }
}
