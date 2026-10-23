package io.github.raghavsatyadev.library.ui.create_match

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.models.db.match_record.TeamDetail
import io.github.raghavsatyadev.library.support.models.essential.CustomError
import io.github.raghavsatyadev.library.support.models.essential.ErrorCode
import io.github.raghavsatyadev.library.support.models.essential.UiState
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateMatchScreenViewModel(
  private val matchRecordRepository: MatchRecordRepository,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _matchRecordFlow = MutableStateFlow<MatchRecord?>(null)
  private val _createMatchRecordEvent = MutableStateFlow<UiState<MatchRecord>>(UiState.Initial)

  val createMatchRecordEvent = _createMatchRecordEvent.asStateFlow()

  fun setMatchRecord(matchRecord: MatchRecord) {
    viewModelScope.launch { _matchRecordFlow.emit(matchRecord) }
  }

  fun resetMatchRecord() {
    viewModelScope.launch { _matchRecordFlow.emit(null) }
  }

  fun saveMatchRecord(
    matchDateTime: Long,
    team1Name: String,
    team2Name: String,
    inningOver: String,
    didTeam1WinToss: Boolean,
    isTeam1BattingFirst: Boolean,
    matchLocation: String,
  ) {
    executeWithLoader {
      try {
        validateMatchDetails(matchLocation, inningOver, team1Name, team2Name)
        val matchRecord =
          MatchRecord(
            location = matchLocation,
            startDateTime = matchDateTime,
            ballsPerInning = inningOver.toInt() * 6,
            team1Detail = TeamDetail(teamName = team1Name),
            team2Detail = TeamDetail(teamName = team2Name),
            didTeam1WonToss = didTeam1WinToss,
            isTeam1BattingFirst = isTeam1BattingFirst,
            localUpdateDateTime = Clock.System.now().toEpochMilliseconds(),
            matchAdminID = "",
          )

        val record = matchRecordRepository.insert(matchRecord)
        _createMatchRecordEvent.emit(UiState.Success(record))
      } catch (e: Exception) {
        _createMatchRecordEvent.emit(UiState.Error(CustomError(ErrorCode.UNKNOWN_ERROR, e)))
      }
    }
  }

  private fun validateMatchDetails(
    matchLocation: String,
    inningOver: String,
    team1Name: String,
    team2Name: String,
  ) {
    when {
      matchLocation.isEmpty() -> throw Exception("Please enter match location")
      inningOver.isEmpty() -> throw Exception("Please enter match overs")
      team1Name.isEmpty() -> throw Exception("Please enter team 1 name")
      team2Name.isEmpty() -> throw Exception("Please enter team 2 name")
    }
  }

  fun createMatchEventConsumed() {
    _createMatchRecordEvent.value = UiState.Initial
  }
}
