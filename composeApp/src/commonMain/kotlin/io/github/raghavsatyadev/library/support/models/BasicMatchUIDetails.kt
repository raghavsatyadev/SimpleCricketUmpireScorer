package io.github.raghavsatyadev.library.support.models

import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus

/**
 * Basic match ui details
 *
 * @property currentTeamName The name of the team currently batting.
 * @property currentRunsAndWickets The current runs and wickets in the format "runs/wickets".
 * @property currentFormattedOvers The current overs formatted as "10.5/50.0"
 * @property currentOvers The current overs in format "10.5"
 * @property totalOvers The total overs allocated for the match.
 * @property currentCRR The current run rate (CRR) of the batting team.
 * @property isFirstInningComplete Indicates if the first inning is complete.
 * @property currentRRR The required run rate (RRR) for the chasing team.
 * @property requiredRunsBalls The required runs and balls remaining in the format "runs (balls)".
 * @property matchStatus The current status of the match [MatchStatus]
 */
data class BasicMatchUIDetails(
  val currentTeamName: String,
  val currentRunsAndWickets: String,
  val currentFormattedOvers: String,
  val currentOvers: String,
  val totalOvers: String,
  val currentCRR: String,
  val isFirstInningComplete: Boolean = false,
  val currentRRR: String = "",
  val requiredRunsBalls: String = "",
  val matchStatus: MatchStatus = MatchStatus.IN_PROGRESS,
)
