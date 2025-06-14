package io.github.raghavsatyadev.scus.compose.ui.create_match

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordComposeDataUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMatchScreenViewModel
@Inject
constructor(
  private val authUtil: FirebaseAuthUtil,
  private val matchRecordComposeDataUtil: MatchRecordComposeDataUtil,
  private val fireStoreUtil: FireStoreUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  val _matchRecordFlow = MutableStateFlow<MatchRecord?>(null)

  init {}

  fun setMatchRecord(matchRecord: MatchRecord) {
    viewModelScope.launch { _matchRecordFlow.emit(matchRecord) }
  }

  fun resetMatchRecord() {
    viewModelScope.launch { _matchRecordFlow.emit(null) }
  }

  fun saveMatchRecord(
    matchDateTime: String,
    team1Name: String,
    team2Name: String,
    inningOver: String,
    selectedIndexToss: Int,
    selectedIndexBat: Int,
    matchLocation: String,
  ) {}
}
