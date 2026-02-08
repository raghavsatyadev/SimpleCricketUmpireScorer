package io.github.raghavsatyadev.scus.ui.dashboard

import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.google.repository.FireStoreRepository
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

class DashboardScreenViewModel(
  private val fireStoreRepository: FireStoreRepository,
  matchRecordDataUtil: MatchRecordDataUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {

  var matchRecordsFlow = matchRecordDataUtil.getAllLive()

  fun deleteMatchRecord(matchRecord: MatchRecord) {
    executeWithLoader { fireStoreRepository.deleteMatchRecord(matchRecord.matchRecordId) }
  }
}
