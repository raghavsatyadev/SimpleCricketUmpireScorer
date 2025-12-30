package io.github.raghavsatyadev.scus.ui.match_complete

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchCompleteScreenViewModel(
    private val matchRecordDataUtil: MatchRecordDataUtil,
    uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _matchRecord = MutableStateFlow<MatchRecord?>(null)
  val matchRecord = _matchRecord.asStateFlow()

  fun loadMatchRecord(matchRecordId: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) { _matchRecord.emit(matchRecordDataUtil.getItem(matchRecordId)) }
    }
  }
}
