package io.github.raghavsatyadev.library.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UiStateManager {
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  fun showLoader() {
    _isLoading.value = true
  }

  fun hideLoader() {
    _isLoading.value = false
  }
}
