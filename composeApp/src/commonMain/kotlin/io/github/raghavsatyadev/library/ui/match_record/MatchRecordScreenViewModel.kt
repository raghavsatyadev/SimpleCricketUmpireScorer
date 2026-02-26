package io.github.raghavsatyadev.library.ui.match_record

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.getRRR
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.isTeam1CurrentlyBatting
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock.System
import io.github.raghavsatyadev.library.support.repository.MatchRecordRepository as MatchRecordDataUtil

class MatchRecordScreenViewModel(
  private val matchRecordDataUtil: MatchRecordDataUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _matchRecordEvent = MutableStateFlow<BasicMatchUIDetails?>(null)
  val matchRecordEvent = _matchRecordEvent.asStateFlow()

  private var loadingJob: Job? = null
  private var currentLoadingMatchId: String? = null

  fun loadMatchRecord(matchRecordId: String) {

    // Update the current match ID being loaded
    currentLoadingMatchId = matchRecordId

    loadingJob =
      viewModelScope.launch {
        try {
          matchRecordDataUtil.getItemLive(matchRecordId).distinctUntilChanged().collectLatest {
            value ->
            // Only emit if this is still the current loading match
            if (currentLoadingMatchId == matchRecordId) {
              _matchRecordEvent.emit(value.toBasicMatchUIDetails())
            }
          }
        } catch (_: Exception) {
          // Handle cancellation gracefully
          if (currentLoadingMatchId == matchRecordId) {
            _matchRecordEvent.emit(null)
          }
        }
      }
  }

  fun clearLoadingState() {
    // Cancel the loading job
    loadingJob?.cancel()

    // Clear the current match ID
    currentLoadingMatchId = null

    // Clear the state
    _matchRecordEvent.value = null
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
            // In second inning, team 2 can only make runs till team 1's runs + 6
            val maxRunsAllowed =
              if (matchRecord.isFirstInningComplete) {
                matchRecord.team1Detail.runs + 6
              } else {
                Int.MAX_VALUE // No limit in first inning
              }

            if (matchRecord.team2Detail.runs + runCount <= maxRunsAllowed) {
              matchRecord.team2Detail.runs += runCount
            } else {
              return@withContext // Reject if exceeds limit
            }
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
        matchRecord.endDateTime = System.now().toEpochMilliseconds()
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }

  fun editTotalOvers(matchRecordID: String, editedOversInBalls: Int) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        val matchRecord = matchRecordDataUtil.getItem(matchRecordID)
        matchRecord.ballsPerInning = editedOversInBalls
        matchRecordDataUtil.update(matchRecord)
      }
    }
  }
}
