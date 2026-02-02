package io.github.raghavsatyadev.support.models.essential

import androidx.annotation.StringRes
import io.github.raghavsatyadev.support.R

enum class ErrorCode(@param:StringRes val warning: Int) {
  UNKNOWN_ERROR(R.string.warning_unknown_error),
  NETWORK_ERROR(R.string.warning_network_error),
  AUTH_FAILED(R.string.warning_auth_failed),
}

fun CustomError.getErrorString(): Int {
  return errorCode.warning
}
