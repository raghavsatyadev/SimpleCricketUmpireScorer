package io.github.raghavsatyadev.library.models.essential

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CustomError(
  val errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR,
  @Contextual val exception: Exception? = null,
)
