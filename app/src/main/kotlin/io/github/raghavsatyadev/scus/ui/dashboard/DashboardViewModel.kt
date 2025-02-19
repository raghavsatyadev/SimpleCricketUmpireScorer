package io.github.raghavsatyadev.scus.ui.dashboard

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.Constants.FieldKeys
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class DashboardViewModel : CoreViewModel() {
    private var getMatchRecordsEvent: MutableStateFlow<Resource<List<MatchRecord>>> =
        MutableStateFlow(Resource.empty())

    fun getMatchRecordsEvent() = getMatchRecordsEvent.asSharedFlow()

    private var copyMatchRecordEvent: MutableStateFlow<Resource<MatchRecord>> =
        MutableStateFlow(Resource.empty())

    fun copyMatchRecordEvent() = copyMatchRecordEvent.asSharedFlow()

    fun loadMatchRecords() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    MatchRecordDataUtil
                        .getInstance()
                        .getAllLive("`${FieldKeys.START_DATE_TIME}` DESC")
                        .collectLatest {
                            getMatchRecordsEvent.emit(Resource.success(it))
                        }
                } catch (e: Exception) {
                    getMatchRecordsEvent.emit(
                        Resource.error(
                            CustomError(
                                ErrorCode.UNKNOWN_ERROR,
                                e
                            )
                        )
                    )
                }
            }
        }
    }

    fun copyMatchRecord(record: MatchRecord) {
        viewModelScope.launch {
            copyMatchRecordEvent.emit(Resource.loading())
            withContext(ioDispatcher) {
                var matchRecord = MatchRecord(
                    location = record.location,
                    startDateTime = Instant
                        .now()
                        .toEpochMilli(),
                    ballsPerInning = record.ballsPerInning,
                    team1Detail = record.team1Detail,
                    team2Detail = record.team2Detail,
                    didTeam1WonToss = record.didTeam1WonToss,
                    isTeam1BattingFirst = record.isTeam1BattingFirst,
                    matchAdminID = record.matchAdminID,
                )
                try {
                    matchRecord = FireStoreUtil
                        .getInstance()
                        .createMatchRecord(matchRecord)
                    copyMatchRecordEvent.emit(Resource.success(matchRecord))
                } catch (e: Exception) {
                    copyMatchRecordEvent.emit(
                        Resource.error(
                            CustomError(
                                ErrorCode.UNKNOWN_ERROR,
                                e
                            )
                        )
                    )
                }
            }
        }
    }

    fun deleteMatchRecord(record: MatchRecord) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val isDeleted = FireStoreUtil
                    .getInstance()
                    .deleteMatchRecord(record.matchRecordId)
                if (isDeleted) {
                    MatchRecordDataUtil
                        .getInstance()
                        .delete(record)
                }
            }
        }
    }
}