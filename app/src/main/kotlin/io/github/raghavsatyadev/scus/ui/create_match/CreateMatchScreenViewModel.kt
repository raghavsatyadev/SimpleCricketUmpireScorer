package io.github.raghavsatyadev.scus.ui.create_match

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.components.UiStateManager
import io.github.raghavsatyadev.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.google.repository.AuthRepository
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.TeamDetail
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.UiState
import io.github.raghavsatyadev.support.providers.StringResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import io.github.raghavsatyadev.support.R as Rs

class CreateMatchScreenViewModel(
  private val authRepository: AuthRepository,
  private val fireStoreUtil: FireStoreUtil,
  private val stringResourceProvider: StringResourceProvider,
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
        val currentUserId = authRepository.currentUserId
        val matchRecord =
          MatchRecord(
            location = matchLocation,
            startDateTime = matchDateTime,
            ballsPerInning = inningOver.toInt() * 6,
            team1Detail = TeamDetail(teamName = team1Name),
            team2Detail = TeamDetail(teamName = team2Name),
            didTeam1WonToss = didTeam1WinToss,
            isTeam1BattingFirst = isTeam1BattingFirst,
            localUpdateDateTime = Date(),
            serverUpdateDateTime = Date(),
            matchAdminID = currentUserId!!,
          )

        val record = fireStoreUtil.createMatchRecord(matchRecord)
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
    val currentUserId = authRepository.currentUserId
    when {
      currentUserId.isNullOrEmpty() ->
        throw Exception(stringResourceProvider.getString(Rs.string.warning_please_login))
      matchLocation.isEmpty() ->
        throw Exception(stringResourceProvider.getString(Rs.string.warning_match_location))
      inningOver.isEmpty() ->
        throw Exception(stringResourceProvider.getString(Rs.string.warning_overs))
      team1Name.isEmpty() ->
        throw Exception(stringResourceProvider.getString(Rs.string.warning_team_1_name))
      team2Name.isEmpty() ->
        throw Exception(stringResourceProvider.getString(Rs.string.warning_team_2_name))
    }
  }

  fun createMatchEventConsumed() {
    _createMatchRecordEvent.value = UiState.Initial
  }
}
