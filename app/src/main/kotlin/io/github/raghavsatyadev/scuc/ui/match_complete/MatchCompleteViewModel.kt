package io.github.raghavsatyadev.scuc.ui.match_complete

import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil

class MatchCompleteViewModel : CoreViewModel() {
    fun getMatchRecord(matchRecordId: Long): MatchRecord {
        return MatchRecordDataUtil.getInstance().getItem(matchRecordId)
    }
}