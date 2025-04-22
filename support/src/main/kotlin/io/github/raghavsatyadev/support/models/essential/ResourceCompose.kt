package io.github.raghavsatyadev.support.models.essential

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ResourceCompose<out T>(
  val code: Int?,
  val status: Status,
  val data: T?,
  val error: CustomError?,
) {

  enum class Status {
    EMPTY,
    SUCCESS,
    ERROR,
  }

  companion object {
    fun <T> success(data: T?): ResourceCompose<T> {
      return ResourceCompose(200, Status.SUCCESS, data, null)
    }

    fun <T> error(error: CustomError?, code: Int? = 400, data: T? = null): ResourceCompose<T> {
      return ResourceCompose(code, Status.ERROR, data, error)
    }

    fun <T> error(
      errorCode: ErrorCode,
      errorMessage: String,
      code: Int? = 400,
      data: T? = null,
    ): ResourceCompose<T> {
      return ResourceCompose(
        code,
        Status.ERROR,
        data,
        CustomError(errorCode, Exception(errorMessage)),
      )
    }

    fun <T> error(
      errorCode: ErrorCode,
      exception: Exception,
      code: Int? = 400,
      data: T? = null,
    ): ResourceCompose<T> {
      return ResourceCompose(code, Status.ERROR, data, CustomError(errorCode, exception))
    }

    fun <T> empty(): ResourceCompose<T> {
      return ResourceCompose(null, Status.EMPTY, null, null)
    }
  }
}
