package io.github.raghavsatyadev.support.compose.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiStateManager @Inject constructor() {
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  fun showLoader() {
    _isLoading.value = true
  }

  fun hideLoader() {
    _isLoading.value = false
  }
}

inline fun <T> UiStateManager.withLoader(
  scope: CoroutineScope,
  crossinline block: suspend () -> T,
) {
  scope.launch {
    showLoader()
    try {
      block()
    } finally {
      hideLoader()
    }
  }
}
