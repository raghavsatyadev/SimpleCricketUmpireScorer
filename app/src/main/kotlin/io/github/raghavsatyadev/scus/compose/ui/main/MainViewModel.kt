package io.github.raghavsatyadev.scus.compose.ui.main

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(uiStateManager: UiStateManager) :
  CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set
}
