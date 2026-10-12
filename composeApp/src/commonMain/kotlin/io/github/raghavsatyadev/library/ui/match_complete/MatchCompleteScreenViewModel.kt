package io.github.raghavsatyadev.library.ui.match_complete

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository as MatchRecordDataUtil
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

  /** Shows [record] at once, until the database copy arrives. Does nothing once one is shown. */
  fun showPreview(record: MatchRecord?) {
    if (record != null && _matchRecord.value == null) _matchRecord.value = record
  }

  fun loadMatchRecord(matchRecordId: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) { _matchRecord.emit(matchRecordDataUtil.getItem(matchRecordId)) }
    }
  }
}
