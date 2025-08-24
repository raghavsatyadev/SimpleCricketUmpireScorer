@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.scus.compose.ui.match_record

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted

@Composable
fun MatchRecordScreen(
  matchId: String,
  matchRecord: MatchRecord,
  viewModel: MatchRecordScreenViewModel = hiltViewModel(),
  onBack: () -> Unit = {},
  onMatchCompleted: () -> Unit = {},
) {
  LaunchedEffect(matchRecord) { viewModel.loadMatchRecord(matchRecord) }

  val recordState by viewModel.matchRecordEvent.collectAsState()

  LaunchedEffect(recordState?.status) {
    if (recordState?.isMatchCompleted() == true) {
      onMatchCompleted()
    }
  }

  val record = recordState

  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      val title =
        record?.let { stringResource(R.string.team) + " " + it.currentTeamName }
          ?: stringResource(R.string.match_record_title)
      AppToolBar(title = title, onNavigateBack = onBack)
    },
  ) { padding ->
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
      val (
        txtRequired,
        txtCrr,
        txtRrr,
        txtRuns,
        txtOvers,
        btnEditOvers,
        btnMinusRun,
        btnAddRun,
        btnMinusBall,
        btnAddBall,
        btnMinusWicket,
        btnAddWicket,
        btnEndInning,
        btnEndMatch,
      ) = createRefs()

      record?.let { details ->
        if (details.isFirstInningComplete) {
          Text(
            text = stringResource(
              R.string.required_runs_balls,
              details.requiredRunsBalls
            ),
            modifier = Modifier.constrainAs(txtRequired) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              top.linkTo(parent.top)
            }
          )
          Text(
            text = stringResource(R.string.crr, details.currentCRR),
            modifier = Modifier.constrainAs(txtCrr) {
              start.linkTo(parent.start)
              top.linkTo(txtRequired.bottom, margin = 8.dp)
            }
          )
          Text(
            text = stringResource(R.string.rrr, details.currentRRR),
            modifier = Modifier.constrainAs(txtRrr) {
              end.linkTo(parent.end)
              top.linkTo(txtRequired.bottom, margin = 8.dp)
            }
          )
        } else {
          Button(
            onClick = { viewModel.endInning(matchId) },
            modifier = Modifier.constrainAs(btnEndInning) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              top.linkTo(parent.top)
            }
          ) { Text(text = stringResource(R.string.end_inning)) }
        }

        Text(
          text = details.currentRunsAndWickets,
          modifier = Modifier.constrainAs(txtRuns) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(txtCrr.bottom, margin = 16.dp)
          }
        )
        Text(
          text = details.currentOvers,
          modifier = Modifier.constrainAs(txtOvers) {
            start.linkTo(parent.start)
            top.linkTo(txtRuns.bottom, margin = 8.dp)
          }
        )
        Button(
          onClick = {
            // simple dialog replacement: increment overs by 1 for demo
            viewModel.editTotalOvers(matchId, details.totalOvers + 1)
          },
          modifier = Modifier.constrainAs(btnEditOvers) {
            start.linkTo(txtOvers.end, margin = 8.dp)
            top.linkTo(txtOvers.top)
          }
        ) { Text(text = stringResource(R.string.edit_total_overs)) }

        ExtendedFloatingActionButton(
          onClick = { viewModel.setRun(matchId, 1, false) },
          modifier = Modifier.constrainAs(btnMinusRun) {
            start.linkTo(parent.start)
            bottom.linkTo(btnAddRun.top, margin = 8.dp)
          },
          text = { Text("-") }
        )
        ExtendedFloatingActionButton(
          onClick = { viewModel.setRun(matchId, 1) },
          modifier = Modifier.constrainAs(btnAddRun) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom)
          },
          text = { Text("+Run") }
        )
        ExtendedFloatingActionButton(
          onClick = { viewModel.setBall(matchId, 1, false) },
          modifier = Modifier.constrainAs(btnMinusBall) {
            end.linkTo(parent.end)
            bottom.linkTo(btnAddBall.top, margin = 8.dp)
          },
          text = { Text("-") }
        )
        ExtendedFloatingActionButton(
          onClick = { viewModel.setBall(matchId, 1) },
          modifier = Modifier.constrainAs(btnAddBall) {
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
          },
          text = { Text("+Ball") }
        )
        ExtendedFloatingActionButton(
          onClick = { viewModel.setWicket(matchId, false) },
          modifier = Modifier.constrainAs(btnMinusWicket) {
            start.linkTo(btnAddRun.end, margin = 16.dp)
            end.linkTo(btnAddBall.start, margin = 16.dp)
            bottom.linkTo(btnAddRun.top, margin = 8.dp)
          },
          text = { Text("-") }
        )
        ExtendedFloatingActionButton(
          onClick = { viewModel.setWicket(matchId) },
          modifier = Modifier.constrainAs(btnAddWicket) {
            start.linkTo(btnAddRun.end, margin = 16.dp)
            end.linkTo(btnAddBall.start, margin = 16.dp)
            bottom.linkTo(btnAddBall.top, margin = 8.dp)
          },
          text = { Text("+W") }
        )
        Button(
          onClick = { viewModel.endMatch(matchId) },
          modifier = Modifier.constrainAs(btnEndMatch) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(txtOvers.bottom, margin = 16.dp)
          }
        ) { Text(text = stringResource(R.string.end_match)) }
      }
    }
  }
}

