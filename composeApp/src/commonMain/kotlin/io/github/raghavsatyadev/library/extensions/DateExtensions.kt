package io.github.raghavsatyadev.library.extensions

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object DateExtensions {
  private val defaultFormat =
    LocalDateTime.Format {
      dayOfMonth()
      char('-')
      monthNumber()
      char('-')
      year()
      char(' ')
      hour() // using 0-23 for simplicity if am/pm is tricky, or check docs.
      // User asked for "dd-MM-yyyy hh:mm a" which is 12 hour.
      // Let's try to match it closely.
    }

  // Better implementation below with 12h support if available or fallback to 24h which is safer
  // cross-platform
  // actually kotlinx-datetime 0.6.0 supports amPmMarker

  private val userDisplayFormat =
    LocalDateTime.Format {
      dayOfMonth()
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

  fun String.formatMillisToDate(dateFormat: String = "dd-MM-yyyy hh:mm a"): String {
    // NOTE: dateFormat argument is ignored in commonMain because we can't parse arbitrary patterns
    // We fall back to the standard userDisplayFormat
    val milliseconds = this.toLongOrNull() ?: return "Invalid date"
    return milliseconds.formatMillisToDate(dateFormat)
  }

  fun Long.formatMillisToDate(dateFormat: String = "dd-MM-yyyy hh:mm a"): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return datetime.format(userDisplayFormat)
  }

  fun String.formatDateToMillis(dateFormat: String = "dd-MM-yyyy hh:mm a"): Long {
    return try {
      val datetime = userDisplayFormat.parse(this)
      datetime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) {
      0L
    }
  }
}
