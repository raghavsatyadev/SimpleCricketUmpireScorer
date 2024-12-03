package io.github.raghavsatyadev.scuc.ui.match_record

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getRRR
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isTeam1CurrentlyBatting
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchRecordViewModel : CoreViewModel() {
    private var matchRecordEvent: MutableStateFlow<Resource<BasicMatchUIDetails>> =
        MutableStateFlow(Resource.empty())

    fun getMatchRecordEvent() = matchRecordEvent.asSharedFlow()

    fun getMatchRecord(record: MatchRecord) {
        matchRecordEvent = MutableStateFlow(Resource.loading())
        viewModelScope.launch {
            withContext(ioDispatcher) {
                record.status = MatchStatus.IN_PROGRESS
                MatchRecordDataUtil.getInstance().update(record)
                MatchRecordDataUtil.getInstance().getItemLive(record.id).collectLatest { value ->
                    matchRecordEvent.emit(Resource.success(value.toBasicMatchUIDetails()))
                }
            }
        }
    }

    fun reset(matchRecordID: Long, resetFull: Boolean = false): Boolean {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord =
                    MatchRecordDataUtil.getInstance().getItem(matchRecordID)
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
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
        return true
    }

    fun setRun(matchRecordID: Long, runCount: Int, increase: Boolean = true) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord = MatchRecordDataUtil.getInstance().getItem(matchRecordID)
                val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
                if (increase) {
                    val firstInningComplete = matchRecord.isFirstInningComplete
                    if (team1CurrentlyBatting) {
                        // handle 2nd batting team's total runs not getting more than 1st batting team's runs +1
                        if (firstInningComplete) {
                            if (matchRecord.team1Detail.runs + runCount > matchRecord.team2Detail.runs + 1) {
                                return@withContext
                            }
                        }
                        matchRecord.team1Detail.runs += runCount
                    } else {
                        // handle 2nd batting team's total runs not getting more than 1st batting team's runs +1
                        if (firstInningComplete) {
                            if (matchRecord.team2Detail.runs + runCount > matchRecord.team1Detail.runs + 1) {
                                return@withContext
                            }
                        }
                        matchRecord.team2Detail.runs += runCount
                    }
                } else {
                    if (team1CurrentlyBatting) {
                        // handle runs not going below 0
                        if (matchRecord.team1Detail.runs - runCount < 0) {
                            return@withContext
                        }
                        matchRecord.team1Detail.runs -= runCount
                    } else {
                        // handle runs not going below 0
                        if (matchRecord.team2Detail.runs - runCount < 0) {
                            return@withContext
                        }
                        matchRecord.team2Detail.runs -= runCount
                    }
                }
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
    }

    fun setWicket(
        matchRecordID: Long,
        increase: Boolean = true,
    ) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord = MatchRecordDataUtil.getInstance().getItem(matchRecordID)
                val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
                if (increase) {
                    if (team1CurrentlyBatting) {
                        matchRecord.team1Detail.wickets++
                    } else {
                        matchRecord.team2Detail.wickets++
                    }
                } else {
                    if (team1CurrentlyBatting) {
                        // handle wickets not going below 0
                        if (matchRecord.team1Detail.wickets - 1 < 0) {
                            return@withContext
                        }
                        matchRecord.team1Detail.wickets--
                    } else {
                        // handle wickets not going below 0
                        if (matchRecord.team2Detail.wickets - 1 < 0) {
                            return@withContext
                        }
                        matchRecord.team2Detail.wickets--
                    }
                }
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
    }

    fun setBall(
        matchRecordID: Long,
        ballCount: Int,
        increase: Boolean = true,
    ) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord = MatchRecordDataUtil.getInstance().getItem(matchRecordID)
                val team1CurrentlyBatting = matchRecord.isTeam1CurrentlyBatting()
                if (increase) {
                    if (team1CurrentlyBatting) {
                        // handle balls not going above ballsPerInning
                        if (matchRecord.team1Detail.balls + ballCount > matchRecord.ballsPerInning) {
                            return@withContext
                        }
                        matchRecord.team1Detail.balls += ballCount
                    } else {
                        // handle balls not going above ballsPerInning
                        if (matchRecord.team2Detail.balls + ballCount > matchRecord.ballsPerInning) {
                            return@withContext
                        }
                        matchRecord.team2Detail.balls += ballCount
                    }
                } else {
                    if (team1CurrentlyBatting) {
                        // handle balls not going below 0
                        if (matchRecord.team1Detail.balls - ballCount < 0) {
                            return@withContext
                        }
                        matchRecord.team1Detail.balls -= ballCount
                    } else {
                        // handle balls not going below 0
                        if (matchRecord.team2Detail.balls - ballCount < 0) {
                            return@withContext
                        }
                        matchRecord.team2Detail.balls -= ballCount
                    }
                }
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
    }

    fun endInning(matchRecordID: Long) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord = MatchRecordDataUtil.getInstance().getItem(matchRecordID)
                matchRecord.isFirstInningComplete = true
                matchRecord.rrrAtSecondInningStart = matchRecord.getRRR()
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
    }

    fun endMatch(matchRecordID: Long) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val matchRecord = MatchRecordDataUtil.getInstance().getItem(matchRecordID)
                when {
                    matchRecord.team1Detail.runs == matchRecord.team2Detail.runs -> matchRecord.status =
                        MatchStatus.DRAW

                    matchRecord.team1Detail.runs > matchRecord.team2Detail.runs -> matchRecord.status =
                        MatchStatus.TEAM_1_WON

                    matchRecord.team1Detail.runs < matchRecord.team2Detail.runs -> matchRecord.status =
                        MatchStatus.TEAM_2_WON
                }
                matchRecord.endDateTime = System.currentTimeMillis()
                MatchRecordDataUtil.getInstance().update(matchRecord)
            }
        }
    }
}