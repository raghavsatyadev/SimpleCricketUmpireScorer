package io.github.raghavsatyadev.scus.compose.ui.dashboard

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordComposeDataUtil
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel
@Inject
constructor(
  private val authUtil: FirebaseAuthUtil,
  matchRecordComposeDataUtil: MatchRecordComposeDataUtil,
  private val fireStoreUtil: FireStoreUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  var matchRecordsFlow = matchRecordComposeDataUtil.getAllLive()

  fun deleteMatchRecord(matchRecord: MatchRecord) {
    executeWithLoader { fireStoreUtil.deleteMatchRecord(matchRecord.matchRecordId) }
  }
}
