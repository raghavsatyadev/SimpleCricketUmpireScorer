package io.github.raghavsatyadev.support.models.essential

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.github.raghavsatyadev.support.extensions.ErrorShowExtensions.snackBar

enum class ErrorCode(@StringRes val warning: Int?) {
  UNKNOWN_ERROR(null),
  AUTH_FAILED(null),
}

fun CustomError.handleError(activity: ComponentActivity) {
  val error = this.getErrorString()
  error?.let { activity.snackBar(it) }
}

fun CustomError.handleError(fragment: Fragment) {
  val error = this.getErrorString()
  error?.let { fragment.snackBar(it) }
}

private fun CustomError.getErrorString(): Int? {
  return errorCode.warning
}
