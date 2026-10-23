package io.github.raghavsatyadev.scus.ui.main

import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.core.CoreScreenViewModel

class MainViewModel(uiStateManager: UiStateManager) : CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set
}
