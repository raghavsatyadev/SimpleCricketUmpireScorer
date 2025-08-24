@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.match_complete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.AppToolBar
import io.github.raghavsatyadev.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails

@Composable
fun MatchCompleteScreen(
  matchId: String,
  viewModel: MatchCompleteScreenViewModel = hiltViewModel(),
  onBack: () -> Unit = {},
) {
  LaunchedEffect(matchId) { viewModel.loadMatchRecord(matchId) }

  val matchRecord by viewModel.matchRecord.collectAsState()

  val team1Details = remember(matchRecord) { matchRecord?.toBasicMatchUIDetails(true) }
  val team2Details = remember(matchRecord) { matchRecord?.toBasicMatchUIDetails(false) }

  MatchCompleteUI(onBack, matchRecord, team1Details, team2Details)
}

@Composable
private fun MatchCompleteUI(
  onBack: () -> Unit,
  matchRecord: MatchRecord?,
  team1Details: BasicMatchUIDetails?,
  team2Details: BasicMatchUIDetails?,
) {
  var showTeam1 by remember { mutableStateOf(true) }
  val details: BasicMatchUIDetails? = if (showTeam1) team1Details else team2Details

  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      AppToolBar(title = stringResource(R.string.match_complete_title), onNavigateBack = onBack)
    },
  ) { padding ->
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      val (
        txtTeam,
        txtRequired,
        txtRrr,
        txtCrr,
        txtRuns,
        txtOvers,
        toggleGroup,
      ) = createRefs()

      details?.let { d ->
        Text(
          text = d.currentTeamName,
          modifier =
            Modifier.constrainAs(txtTeam) {
              top.linkTo(parent.top)
              start.linkTo(parent.start)
              end.linkTo(parent.end)
            },
        )
        matchRecord?.let { record ->
          val isBattingFirst = record.isTeam1BattingFirst
          val showRequired = (showTeam1 && !isBattingFirst) || (!showTeam1 && isBattingFirst)
          if (showRequired) {
            Text(
              text = stringResource(R.string.required_runs_balls_at_end, d.requiredRunsBalls),
              modifier =
                Modifier.constrainAs(txtRequired) {
                  start.linkTo(parent.start)
                  end.linkTo(parent.end)
                  top.linkTo(txtTeam.bottom, margin = 16.dp)
                },
            )
            Text(
              text = stringResource(R.string.rrr_at_start, record.rrrAtSecondInningStart),
              modifier =
                Modifier.constrainAs(txtRrr) {
                  start.linkTo(parent.start)
                  top.linkTo(txtRequired.bottom, margin = 8.dp)
                },
            )
            Text(
              text = stringResource(R.string.crr, d.currentCRR),
              modifier =
                Modifier.constrainAs(txtCrr) {
                  end.linkTo(parent.end)
                  top.linkTo(txtRequired.bottom, margin = 8.dp)
                },
            )
          } else {
            Text(
              text = stringResource(R.string.crr, d.currentCRR),
              modifier =
                Modifier.constrainAs(txtCrr) {
                  start.linkTo(parent.start)
                  end.linkTo(parent.end)
                  top.linkTo(txtTeam.bottom, margin = 16.dp)
                },
            )
          }
        }
        Text(
          text = d.currentRunsAndWickets,
          modifier =
            Modifier.constrainAs(txtRuns) {
              top.linkTo(txtCrr.bottom, margin = 16.dp)
              start.linkTo(parent.start)
              end.linkTo(parent.end)
            },
        )
        Text(
          text = d.currentOvers,
          modifier =
            Modifier.constrainAs(txtOvers) {
              top.linkTo(txtRuns.bottom, margin = 8.dp)
              start.linkTo(parent.start)
              end.linkTo(parent.end)
            },
        )
      }

      FlowRow(
        modifier =
          Modifier.constrainAs(toggleGroup) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
          },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
      ) {
        ToggleButton(
          checked = showTeam1,
          onCheckedChange = { showTeam1 = true },
          modifier = Modifier.weight(1f),
          shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
        ) {
          Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          Text(text = stringResource(R.string.team_1))
        }
        ToggleButton(
          checked = !showTeam1,
          onCheckedChange = { showTeam1 = false },
          modifier = Modifier.weight(1f),
          shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
        ) {
          Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          Text(text = stringResource(R.string.team_2))
        }
      }
    }
  }
}
