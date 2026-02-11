package io.github.raghavsatyadev.library.models.essential

import kotlinx.serialization.Serializable

@Serializable
sealed class UiState<out T> {

  @Serializable data class Success<out T>(val data: T) : UiState<T>()

  @Serializable data class Error(val error: CustomError, val code: Int = 400) : UiState<Nothing>()

  @Serializable data object Initial : UiState<Nothing>()
}
