package io.github.raghavsatyadev.library.support.extensions

import java.util.Locale

actual fun Double.format(digits: Int): String =
  String.format(Locale.getDefault(), "%.${digits}f", this)
