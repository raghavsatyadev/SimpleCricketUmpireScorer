package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.runtime.Composable
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.TeamDetail

@Composable
fun MatchRecordItem(matchRecord: MatchRecord, onCopyClick: () -> Unit, onDeleteClick: () -> Unit) {}

@LightPreview
@Composable
fun MatchRecordItemPreview() {
  MatchRecordItem(
    matchRecord =
      MatchRecord(
        startDateTime = System.currentTimeMillis(),
        team1Detail = TeamDetail(teamName = "Team 1"),
        team2Detail = TeamDetail(teamName = "Team 2"),
        ballsPerInning = 120,
        matchAdminID = "",
      ),
    onCopyClick = {},
    onDeleteClick = {},
  )
}

@LightPreview
@Composable
fun MatchRecordItemPreview2() {

  MatchRecordItem(
    matchRecord =
      MatchRecord(
        startDateTime = System.currentTimeMillis(),
        team1Detail = TeamDetail(teamName = "Team 1"),
        team2Detail = TeamDetail(teamName = "Team 2"),
        ballsPerInning = 120,
        matchAdminID = "",
      ),
    onCopyClick = {},
    onDeleteClick = {},
  )
}
