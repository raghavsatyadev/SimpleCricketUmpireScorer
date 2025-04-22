package io.github.raghavsatyadev.support.compose.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
