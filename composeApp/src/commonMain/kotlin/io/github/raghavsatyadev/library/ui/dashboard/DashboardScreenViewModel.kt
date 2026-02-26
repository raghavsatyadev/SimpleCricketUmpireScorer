package io.github.raghavsatyadev.library.ui.dashboard

import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.google.repository.FireStoreRepository
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository as MatchRecordDataUtil

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
