package io.github.raghavsatyadev.scuc.ui.dashboard

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.Constants.FieldKeys
import io.github.raghavsatyadev.support.core.CoreViewModel
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
    private var matchRecordsEvent: MutableStateFlow<Resource<List<MatchRecord>>> =
        MutableStateFlow(Resource.empty())

    fun getMatchRecordsEvent() = matchRecordsEvent.asSharedFlow()

    fun loadMatchRecords() {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                try {
                    MatchRecordDataUtil
                        .getInstance()
                        .getAllLive("`${FieldKeys.START_DATE_TIME}` DESC")
                        .collectLatest {
                            matchRecordsEvent.emit(Resource.success(it))
                        }
                } catch (e: Exception) {
                    matchRecordsEvent.emit(
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
}