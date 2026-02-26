package io.github.raghavsatyadev.library.ui.main

import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.google.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(uiStateManager: UiStateManager, private val authRepository: AuthRepository) :
  CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set

  private var _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn())
  val isLoggedIn = _isLoggedIn.asStateFlow()

  fun changeLoginState() {
    _isLoggedIn.value = authRepository.isLoggedIn()
  }
}
