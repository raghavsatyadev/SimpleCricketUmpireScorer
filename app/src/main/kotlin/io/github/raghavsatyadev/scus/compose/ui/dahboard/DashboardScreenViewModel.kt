package io.github.raghavsatyadev.scus.compose.ui.dahboard

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel
@Inject
constructor(private val authUtil: FirebaseAuthUtil, uiStateManager: UiStateManager) :
  CoreScreenViewModel(uiStateManager) {
  fun isLoggedIn(): Boolean {
    return authUtil.isLoggedIn()
  }
}
