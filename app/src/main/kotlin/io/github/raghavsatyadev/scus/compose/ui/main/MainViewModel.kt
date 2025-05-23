package io.github.raghavsatyadev.scus.compose.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    uiStateManager: UiStateManager,
    private val authUtil: FirebaseAuthUtil,
) :
  CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set

    val isLoggedIn = authUtil.isLoggedIn
}
