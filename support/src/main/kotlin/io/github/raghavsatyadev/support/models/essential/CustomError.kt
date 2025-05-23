package io.github.raghavsatyadev.support.models.essential

import androidx.annotation.Keep
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class CustomError(
  val errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR,
  @Contextual
  val exception: Exception? = null,
)
