package io.github.raghavsatyadev.support.models

import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus

data class BasicMatchUIDetails(
    val currentTeamName: String,
    val currentRunsAndWickets: String,
    val currentOvers: String,
    val currentCRR: String,
    val isFirstInningComplete: Boolean = false,
    val currentRRR: String = "",
    val requiredRunsBalls: String = "",
    val matchStatus: MatchStatus = MatchStatus.IN_PROGRESS,
)