package io.github.raghavsatyadev.scus.ui.main

import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(uiStateManager: UiStateManager, private val authUtil: FirebaseAuthUtil) :
  CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set

  private var _isLoggedIn = MutableStateFlow(authUtil.isLoggedIn.value)
  val isLoggedIn = _isLoggedIn.asStateFlow()

  fun changeLoginState() {
    _isLoggedIn.value = authUtil.isLoggedIn.value
  }
}
