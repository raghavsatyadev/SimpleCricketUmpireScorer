package io.github.raghavsatyadev.scus.compose.ui.match_record

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordComposeDataUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getRRR
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isTeam1CurrentlyBatting
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MatchRecordScreenViewModel @Inject constructor(
  private val matchRecordDataUtil: MatchRecordComposeDataUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _matchRecordEvent = MutableStateFlow<BasicMatchUIDetails?>(null)
  val matchRecordEvent = _matchRecordEvent.asStateFlow()

  fun loadMatchRecord(matchRecord: MatchRecord) {
    viewModelScope.launch {
      matchRecordDataUtil.getItemLive(matchRecord.matchRecordId).collectLatest { value ->
        _matchRecordEvent.emit(value.toBasicMatchUIDetails())
      }
    }
  }

  fun reset(matchRecordID: String, resetFull: Boolean = false) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        if (resetFull) {
          matchRecord.team1Detail.runs = 0
          matchRecord.team1Detail.wickets = 0
          matchRecord.team1Detail.balls = 0
          matchRecord.team2Detail.runs = 0
          matchRecord.team2Detail.wickets = 0
          matchRecord.team2Detail.balls = 0
          matchRecord.isFirstInningComplete = false
        } else {
          if (matchRecord.isTeam1CurrentlyBatting()) {
            matchRecord.team1Detail.runs = 0
            matchRecord.team1Detail.wickets = 0
            matchRecord.team1Detail.balls = 0
          } else {
            matchRecord.team2Detail.runs = 0
            matchRecord.team2Detail.wickets = 0
            matchRecord.team2Detail.balls = 0
          }
        }
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun setRun(matchRecordID: String, runCount: Int, increase: Boolean = true) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
        if (increase) {
          if (team1CurrentlyBatting) {
            matchRecord.team1Detail.runs += runCount
          } else {
            matchRecord.team2Detail.runs += runCount
          }
        } else {
          if (team1CurrentlyBatting) {
            if (matchRecord.team1Detail.runs - runCount < 0) return@withContext
            matchRecord.team1Detail.runs -= runCount
          } else {
            if (matchRecord.team2Detail.runs - runCount < 0) return@withContext
            matchRecord.team2Detail.runs -= runCount
          }
        }
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun setWicket(matchRecordID: String, increase: Boolean = true) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
        if (increase) {
          if (team1CurrentlyBatting) {
            matchRecord.team1Detail.wickets++
          } else {
            matchRecord.team2Detail.wickets++
          }
        } else {
          if (team1CurrentlyBatting) {
            if (matchRecord.team1Detail.wickets - 1 < 0) return@withContext
            matchRecord.team1Detail.wickets--
          } else {
            if (matchRecord.team2Detail.wickets - 1 < 0) return@withContext
            matchRecord.team2Detail.wickets--
          }
        }
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun setBall(matchRecordID: String, ballCount: Int, increase: Boolean = true) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
        if (increase) {
          if (team1CurrentlyBatting) {
            if (matchRecord.team1Detail.balls + ballCount > matchRecord.ballsPerInning) {
              return@withContext
            }
            matchRecord.team1Detail.balls += ballCount
          } else {
            if (matchRecord.team2Detail.balls + ballCount > matchRecord.ballsPerInning) {
              return@withContext
            }
            matchRecord.team2Detail.balls += ballCount
          }
        } else {
          if (team1CurrentlyBatting) {
            if (matchRecord.team1Detail.balls - ballCount < 0) return@withContext
            matchRecord.team1Detail.balls -= ballCount
          } else {
            if (matchRecord.team2Detail.balls - ballCount < 0) return@withContext
            matchRecord.team2Detail.balls -= ballCount
          }
        }
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun endInning(matchRecordID: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        matchRecord.isFirstInningComplete = true
        matchRecord.rrrAtSecondInningStart = matchRecord.getRRR()
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun endMatch(matchRecordID: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        matchRecord.status =
          when {
            matchRecord.team1Detail.runs == matchRecord.team2Detail.runs -> MatchStatus.DRAW
            matchRecord.team1Detail.runs > matchRecord.team2Detail.runs -> MatchStatus.TEAM_1_WON
            else -> MatchStatus.TEAM_2_WON
          }
        matchRecord.endDateTime = System.currentTimeMillis()
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun editTotalOvers(matchRecordID: String, editedOvers: Int) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        matchRecord.ballsPerInning = editedOvers * 6
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }
}

