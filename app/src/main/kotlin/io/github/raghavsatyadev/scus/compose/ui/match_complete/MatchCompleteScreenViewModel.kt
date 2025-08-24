package io.github.raghavsatyadev.scus.compose.ui.match_complete

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordComposeDataUtil
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MatchCompleteScreenViewModel @Inject constructor(
  private val matchRecordDataUtil: MatchRecordComposeDataUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _matchRecord = MutableStateFlow<MatchRecord?>(null)
  val matchRecord = _matchRecord.asStateFlow()

  fun loadMatchRecord(matchRecordId: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        _matchRecord.emit(matchRecordDataUtil.getItem(matchRecordId))
      }
    }
  }
}

