@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.DarkPreview
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.compose.theme.AppTheme
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getMatchTimings
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getTeam1FormattedScore
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.getTeam2FormattedScore
import io.github.raghavsatyadev.support.models.db.match_record.MatchStatus

@LightPreview
@DarkPreview
@Composable
fun MatchRecordItemPreview() {
  AppTheme {
    val matchRecord = getSampleMatchRecord(1)
    MatchRecordProperties.CreateMatchRecordProperties { properties ->
      MatchRecordItem(
        matchRecord = matchRecord,
        properties = properties,
        onCopyClick = {},
        onDeleteClick = {},
        onMatchClick = {},
        modifier =
          Modifier.padding(vertical = 8.dp, horizontal = 16.dp).fillMaxWidth().wrapContentHeight(),
      )
    }
  }
}

@Composable
fun MatchRecordItem(
  modifier: Modifier,
  matchRecord: MatchRecord,
  properties: MatchRecordProperties,
  onCopyClick: (MatchRecord) -> Unit,
  onDeleteClick: (MatchRecord) -> Unit,
  onMatchClick: (MatchRecord) -> Unit,
) {
  ElevatedCard(
    shape = MaterialTheme.shapes.medium,
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = modifier.clickable { onMatchClick(matchRecord) },
  ) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp)) {
      val midVerticalGuideLine = createGuidelineFromStart(0.5f)
      val (
        txtCombinedStatus,
        txtTeam1Name,
        txtTeam1Status,
        txtTeam2Name,
        txtTeam2Status,
        txtTeam1Score,
        txtTeam2Score,
        txtMatchLocation,
        txtMatchDuration,
        btnCopy,
        btnDelete,
        separatorTeam,
      ) = createRefs()

      createHorizontalChain(btnCopy, btnDelete)

      val team1Score = matchRecord.getTeam1FormattedScore()
      val team2Score = matchRecord.getTeam2FormattedScore()
      var team1Status = ""
      var team2Status = ""
      var team1StatusColor: Color = Color.White
      var team2StatusColor: Color = Color.White
      var combinedStatus = ""
      var combinedStatusColor: Color = Color.White
      var shouldShowCombined: Boolean

      when (matchRecord.status) {
        MatchStatus.TEAM_1_WON -> {
          team1Status = properties.won
          team2Status = properties.lost
          team1StatusColor = properties.winColor
          team2StatusColor = properties.lostColor
          shouldShowCombined = false
        }

        MatchStatus.TEAM_2_WON -> {
          team1Status = properties.lost
          team2Status = properties.won
          team1StatusColor = properties.lostColor
          team2StatusColor = properties.winColor
          shouldShowCombined = false
        }

        MatchStatus.DRAW -> {
          shouldShowCombined = true
          combinedStatus = properties.draw
          combinedStatusColor = properties.drawColor
        }

        else -> {
          shouldShowCombined = true
          combinedStatus = properties.inProgress
          combinedStatusColor = properties.inProgressColor
        }
      }

      val statusBarrier =
        createBottomBarrier(txtCombinedStatus, txtTeam1Status, txtTeam2Status, margin = 8.dp)
      val teamBarrier = createTopBarrier(txtTeam1Name, txtTeam2Name, txtTeam1Status, txtTeam2Status)
      Spacer(
        modifier =
          Modifier.constrainAs(separatorTeam) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              top.linkTo(teamBarrier)
              bottom.linkTo(txtTeam1Score.bottom)
              width = Dimension.value(1.dp)
              height = Dimension.fillToConstraints
            }
            .background(MaterialTheme.colorScheme.outline)
      )
      val combineStatusVisibility =
        if (shouldShowCombined) {
          Visibility.Visible
        } else {
          Visibility.Gone
        }
      val separateStatusVisibility =
        if (shouldShowCombined) {
          Visibility.Gone
        } else {
          Visibility.Visible
        }
      Text(
        modifier =
          Modifier.constrainAs(txtCombinedStatus) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            visibility = combineStatusVisibility
          },
        text = combinedStatus,
        fontWeight = FontWeight.Bold,
        color = combinedStatusColor,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = team1Status,
        modifier =
          Modifier.constrainAs(txtTeam1Status) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(txtCombinedStatus.bottom)
            visibility = separateStatusVisibility
          },
        color = team1StatusColor,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = team2Status,
        modifier =
          Modifier.constrainAs(txtTeam2Status) {
            end.linkTo(parent.end)
            start.linkTo(midVerticalGuideLine)
            top.linkTo(txtCombinedStatus.bottom)
            visibility = separateStatusVisibility
          },
        fontWeight = FontWeight.ExtraBold,
        color = team2StatusColor,
        style = MaterialTheme.typography.titleMedium,
      )

      Text(
        text = matchRecord.team1Detail.teamName,
        modifier =
          Modifier.constrainAs(txtTeam1Name) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(statusBarrier)
          },
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.labelLarge,
      )
      Text(
        text = matchRecord.team2Detail.teamName,
        modifier =
          Modifier.constrainAs(txtTeam2Name) {
            start.linkTo(midVerticalGuideLine)
            end.linkTo(parent.end)
            top.linkTo(statusBarrier)
          },
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.labelLarge,
      )
      Text(
        text = team1Score,
        style = MaterialTheme.typography.bodyMedium,
        modifier =
          Modifier.constrainAs(txtTeam1Score) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(txtTeam1Name.bottom)
          },
      )
      Text(
        text = team2Score,
        style = MaterialTheme.typography.bodyMedium,
        modifier =
          Modifier.constrainAs(txtTeam2Score) {
            start.linkTo(midVerticalGuideLine)
            end.linkTo(parent.end)
            top.linkTo(txtTeam2Name.bottom)
          },
      )
      Text(
        text = matchRecord.location,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier =
          Modifier.constrainAs(txtMatchLocation) {
            start.linkTo(parent.start)
            top.linkTo(txtTeam1Score.bottom, 10.dp)
          },
      )
      Text(
        text = matchRecord.getMatchTimings(),
        style = MaterialTheme.typography.bodyMedium,
        modifier =
          Modifier.constrainAs(txtMatchDuration) {
            start.linkTo(parent.start)
            top.linkTo(txtMatchLocation.bottom)
          },
      )
      OutlinedIconButton(
        onClick = { onCopyClick(matchRecord) },
        modifier = Modifier.constrainAs(btnCopy) { top.linkTo(txtMatchDuration.bottom, 10.dp) },
      ) {
        Icon(
          tint = MaterialTheme.colorScheme.primary,
          painter = painterResource(id = R.drawable.ic_copy),
          contentDescription = stringResource(R.string.copy_match_record),
        )
      }
      OutlinedIconButton(
        onClick = { onDeleteClick(matchRecord) },
        modifier = Modifier.constrainAs(btnDelete) { top.linkTo(txtMatchDuration.bottom, 10.dp) },
      ) {
        Icon(
          tint = MaterialTheme.colorScheme.primary,
          painter = painterResource(id = R.drawable.ic_delete),
          contentDescription = stringResource(R.string.delete_match_record),
        )
      }
    }
  }
}

fun getSampleMatchRecord(i: Int): MatchRecord {
  return "{\"match_record_id\":\"eHBkXOi9Gxzqsd8dSP0D$i\",\"start_date_time\":1745345640000,\"end_date_time\":1745589475307,\"team_1\":{\"team_name\":\"Raghav\",\"runs\":21,\"wickets\":7,\"balls\":22},\"team_2\":{\"team_name\":\"Archan\",\"runs\":18,\"balls\":18},\"balls_per_inning\":72,\"is_first_inning_complete\":true,\"rrr_at_second_inning_start\":\"1.83\",\"status\":\"TEAM_1_WON\",\"location\":\"Ahmedabad \",\"match_admin_id\":\"r4pkT36tARfXSv2OLx1qP8xfWzl1\",\"local_update_date_time\":1745589475307,\"server_update_date_time\":1745345691000}"
    .toKotlinObject()
}

fun getSampleRecords(): List<MatchRecord> {
  return buildList { repeat(10) { add(getSampleMatchRecord(it)) } }
}
