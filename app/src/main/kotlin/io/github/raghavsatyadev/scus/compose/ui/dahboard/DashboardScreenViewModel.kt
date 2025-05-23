package io.github.raghavsatyadev.scus.compose.ui.dahboard

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel
@Inject
constructor(private val authUtil: FirebaseAuthUtil, uiStateManager: UiStateManager) :
  CoreScreenViewModel(uiStateManager) {
    private var _matchRecordsFlow: MutableStateFlow<Resource<List<MatchRecord>>> =
        MutableStateFlow(Resource.empty())

    val matchRecordsFlow = _matchRecordsFlow.asStateFlow()

  fun isLoggedIn(): Boolean {
    return authUtil.isLoggedIn()
  }

  fun deleteMatchRecord(matchRecord: MatchRecord) {

  }
}
