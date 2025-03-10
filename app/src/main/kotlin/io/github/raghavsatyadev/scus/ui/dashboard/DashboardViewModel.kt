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

class DashboardViewModel : CoreViewModel() {
    private var getMatchRecordsEvent: MutableStateFlow<Resource<List<MatchRecord>>> =
        MutableStateFlow(Resource.empty())

    fun getMatchRecordsEvent() = getMatchRecordsEvent.asSharedFlow()

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