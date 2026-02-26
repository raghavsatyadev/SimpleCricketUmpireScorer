package io.github.raghavsatyadev.library.ui.match_complete

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository as MatchRecordDataUtil

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
