package io.github.raghavsatyadev.library.ui.main

import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel

class MainViewModel(uiStateManager: UiStateManager) : CoreScreenViewModel(uiStateManager) {
  var isLoading = uiStateManager.isLoading
    private set
}
