package io.github.raghavsatyadev.support.models.essential

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Resource<out T>(
    val code: Int?,
    val status: Status,
    val data: T?,
    val error: CustomError?,
) {

    enum class Status {
        EMPTY,
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(
                200, Status.SUCCESS, data, null
            )
        }

        fun <T> error(
            error: CustomError?,
            code: Int? = 400,
            data: T? = null,
        ): Resource<T> {
            return Resource(
                code, Status.ERROR, data, error
            )
        }

        fun <T> error(
            errorCode: ErrorCode,
            errorMessage: String,
            code: Int? = 400,
            data: T? = null,
        ): Resource<T> {
            return Resource(
                code, Status.ERROR, data, CustomError(errorCode, Exception(errorMessage))
            )
        }

        fun <T> error(
            errorCode: ErrorCode,
            exception: Exception,
            code: Int? = 400,
            data: T? = null,
        ): Resource<T> {
            return Resource(
                code, Status.ERROR, data, CustomError(errorCode, exception)
            )
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(
                100, Status.LOADING, data, null
            )
        }

        fun <T> empty(): Resource<T> {
            return Resource(
                null, Status.EMPTY, null, null
            )
        }
    }
}