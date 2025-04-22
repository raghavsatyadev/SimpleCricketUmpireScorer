package io.github.raghavsatyadev.support.compose.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class CoreScreenViewModel(protected val uiStateManager: UiStateManager) : ViewModel() {
  val mainDispatcher = Dispatchers.Main
  val ioDispatcher = Dispatchers.IO
  val defaultDispatcher = Dispatchers.Default

  /**
   * Wraps any suspend block with global loader toggling – shows loader before launching, hides it
   * on completion (success or error).
   */
  protected fun executeWithLoader(block: suspend () -> Unit) {
    viewModelScope.launch {
      uiStateManager.showLoader()
      try {
          withContext(ioDispatcher) { block() }
      } finally {
        uiStateManager.hideLoader()
      }
    }
  }
}