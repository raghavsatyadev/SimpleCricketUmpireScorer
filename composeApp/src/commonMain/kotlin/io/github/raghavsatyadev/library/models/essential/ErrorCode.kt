package io.github.raghavsatyadev.library.models.essential

enum class ErrorCode(val warning: String) {
  UNKNOWN_ERROR("Unknown Error"),
  NETWORK_ERROR("Please connect to a stable internet connection"),
  AUTH_FAILED("Authentication failed"),
}

fun CustomError.getErrorString(): String {
  return errorCode.warning
}
