@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import io.github.raghavsatyadev.library.support.components.DarkPreview
import io.github.raghavsatyadev.library.support.components.LightPreview
import io.github.raghavsatyadev.library.support.components.StatusPill
import io.github.raghavsatyadev.library.support.components.StatusTone
import io.github.raghavsatyadev.library.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.getMatchTimings
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.getTeam1FormattedScore
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.getTeam2FormattedScore
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import io.github.raghavsatyadev.library.support.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.copy_match_record
import scus.composeapp.generated.resources.delete_match_record
import scus.composeapp.generated.resources.ic_copy
import scus.composeapp.generated.resources.ic_delete

@LightPreview
@DarkPreview
@Composable
fun MatchRecordItemPreview() {
  AppTheme {
    val matchRecord = getSampleMatchRecord(1)
    val properties = MatchRecordProperties.rememberMatchRecordProperties()
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

@Composable
fun MatchRecordItem(
  modifier: Modifier,
  matchRecord: MatchRecord,
  properties: MatchRecordProperties,
  onCopyClick: (MatchRecord) -> Unit,
  onDeleteClick: (MatchRecord) -> Unit,
  onMatchClick: (MatchRecord) -> Unit,
) {
  val motion = MaterialTheme.motionScheme
  val copyShape =
    RoundedCornerShape(
      topStartPercent = 50,
      bottomStartPercent = 50,
      topEndPercent = 15,
      bottomEndPercent = 15,
    )
  val deleteShape =
    RoundedCornerShape(
      topStartPercent = 15,
      bottomStartPercent = 15,
      topEndPercent = 50,
      bottomEndPercent = 50,
    )
  Card(
    onClick = { onMatchClick(matchRecord) },
    modifier = modifier.animateContentSize(motion.defaultSpatialSpec()),
    shape = MaterialTheme.shapes.large,
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    elevation = CardDefaults.cardElevation(),
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

      createHorizontalChain(btnCopy, btnDelete, chainStyle = ChainStyle.Packed)

      val team1Score = matchRecord.getTeam1FormattedScore()
      val team2Score = matchRecord.getTeam2FormattedScore()
      var team1Status = ""
      var team2Status = ""
      var team1Tone = StatusTone.Neutral
      var team2Tone = StatusTone.Neutral
      var combinedStatus = ""
      var combinedTone = StatusTone.Neutral
      var shouldShowCombined: Boolean

      when (matchRecord.status) {
        MatchStatus.TEAM_1_WON -> {
          team1Status = properties.won
          team2Status = properties.lost
          team1Tone = StatusTone.Positive
          team2Tone = StatusTone.Negative
          shouldShowCombined = false
        }
        MatchStatus.TEAM_2_WON -> {
          team1Status = properties.lost
          team2Status = properties.won
          team1Tone = StatusTone.Negative
          team2Tone = StatusTone.Positive
          shouldShowCombined = false
        }
        MatchStatus.DRAW -> {
          shouldShowCombined = true
          combinedStatus = properties.draw
          combinedTone = StatusTone.Neutral
        }
        else -> {
          shouldShowCombined = true
          combinedStatus = properties.inProgress
          combinedTone = StatusTone.Active
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
            .background(MaterialTheme.colorScheme.outlineVariant)
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
      StatusPill(
        text = combinedStatus,
        tone = combinedTone,
        modifier =
          Modifier.constrainAs(txtCombinedStatus) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            visibility = combineStatusVisibility
          },
      )
      StatusPill(
        text = team1Status,
        tone = team1Tone,
        modifier =
          Modifier.constrainAs(txtTeam1Status) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(txtCombinedStatus.bottom)
            visibility = separateStatusVisibility
          },
      )
      StatusPill(
        text = team2Status,
        tone = team2Tone,
        modifier =
          Modifier.constrainAs(txtTeam2Status) {
            end.linkTo(parent.end)
            start.linkTo(midVerticalGuideLine)
            top.linkTo(txtCombinedStatus.bottom)
            visibility = separateStatusVisibility
          },
      )

      Text(
        text = matchRecord.team1Detail.teamName,
        modifier =
          Modifier.constrainAs(txtTeam1Name) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(statusBarrier)
          },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
      )
      Text(
        text = matchRecord.team2Detail.teamName,
        modifier =
          Modifier.constrainAs(txtTeam2Name) {
            start.linkTo(midVerticalGuideLine)
            end.linkTo(parent.end)
            top.linkTo(statusBarrier)
          },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
      )
      Text(
        text = team1Score,
        style = MaterialTheme.typography.titleMedium,
        modifier =
          Modifier.constrainAs(txtTeam1Score) {
            start.linkTo(parent.start)
            end.linkTo(midVerticalGuideLine)
            top.linkTo(txtTeam1Name.bottom)
          },
      )
      Text(
        text = team2Score,
        style = MaterialTheme.typography.titleMedium,
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
      FilledTonalIconButton(
        onClick = { onCopyClick(matchRecord) },
        shapes =
          IconButtonDefaults.shapes(shape = copyShape, pressedShape = MaterialTheme.shapes.small),
        modifier = Modifier.constrainAs(btnCopy) { top.linkTo(txtMatchDuration.bottom, 10.dp) },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_copy),
          contentDescription = stringResource(Res.string.copy_match_record),
        )
      }
      FilledTonalIconButton(
        onClick = { onDeleteClick(matchRecord) },
        shapes =
          IconButtonDefaults.shapes(shape = deleteShape, pressedShape = MaterialTheme.shapes.small),
        colors =
          IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
          ),
        modifier =
          Modifier.constrainAs(btnDelete) {
            top.linkTo(txtMatchDuration.bottom, 10.dp)
            start.linkTo(btnCopy.end, margin = ButtonGroupDefaults.ConnectedSpaceBetween)
          },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_delete),
          contentDescription = stringResource(Res.string.delete_match_record),
        )
      }
    }
  }
}

fun getSampleMatchRecord(i: Int): MatchRecord {
  return "{\"match_record_id\":\"eHBkXOi9Gxzqsd8dSP0D\$i\",\"start_date_time\":1745345640000,\"end_date_time\":1745589475307,\"team_1\":{\"team_name\":\"Raghav\",\"runs\":21,\"wickets\":7,\"balls\":22},\"team_2\":{\"team_name\":\"Archan\",\"runs\":18,\"balls\":18},\"balls_per_inning\":72,\"is_first_inning_complete\":true,\"rrr_at_second_inning_start\":\"1.83\",\"status\":\"TEAM_1_WON\",\"location\":\"Ahmedabad \",\"match_admin_id\":\"r4pkT36tARfXSv2OLx1qP8xfWzl1\",\"local_update_date_time\":1745589475307,\"server_update_date_time\":1745345691000}"
    .toKotlinObject()
}

fun getSampleRecords(): List<MatchRecord> {
  return buildList { repeat(10) { add(getSampleMatchRecord(it)) } }
}
