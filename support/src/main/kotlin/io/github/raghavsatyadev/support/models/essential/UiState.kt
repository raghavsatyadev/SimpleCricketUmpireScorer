package io.github.raghavsatyadev.support.models.essential

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class UiState<out T> {

    @Keep
    @Serializable
    data class Success<out T>(val data: T) : UiState<T>()

    @Keep
    @Serializable
    data class Error(
        val error: CustomError,
        val code: Int = 400,
    ) : UiState<Nothing>()

    @Keep
    @Serializable
    data object Initial : UiState<Nothing>()
}
