@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.match_record

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
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

  LaunchedEffect(recordState?.matchStatus) {
    if (recordState?.isMatchCompleted() == true) {
      onMatchCompleted()
    }
  }

  val record = recordState

  MatchRecordUI(
    record = record,
    onBack = onBack,
    endInning = { viewModel.endInning(matchId) },
    endMatch = { viewModel.endMatch(matchId) },
    setWicket = { viewModel.setWicket(matchId, it) },
    setBall = { balls, isAdd -> viewModel.setBall(matchId, balls, isAdd) },
    setRun = { runs, isAdd -> viewModel.setRun(matchId, runs, isAdd) },
  )
}

@Composable
private fun MatchRecordUI(
  record: BasicMatchUIDetails?,
  onBack: () -> Unit,
  endMatch: () -> Unit,
  endInning: () -> Unit,
  setWicket: (Boolean) -> Unit,
  setBall: (Int, Boolean) -> Unit,
  setRun: (Int, Boolean) -> Unit,
) {
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
        labelWickets,
        btnMinusWicket,
        btnAddWicket,
        labelOvers,
        btnMinusBall,
        btnAddBall,
        btnEndInning,
        btnEndMatch,
      ) = createRefs()
      val (
        labelRuns,
        btnMinusRun,
        btnAddRun,
      ) = createRefs()

      record?.let { details ->
        if (details.isFirstInningComplete) {
          Button(
            onClick = endMatch,
            modifier =
              Modifier.constrainAs(btnEndMatch) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
              },
          ) {
            Text(text = stringResource(R.string.end_match))
          }

          Text(
            text = stringResource(R.string.required_runs_balls, details.requiredRunsBalls),
            modifier =
              Modifier.constrainAs(txtRequired) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(btnEndMatch.bottom, margin = 16.dp)
              },
          )
          Text(
            text = stringResource(R.string.crr, details.currentCRR),
            modifier =
              Modifier.constrainAs(txtCrr) {
                start.linkTo(parent.start)
                top.linkTo(txtRequired.bottom, margin = 8.dp)
              },
          )
          Text(
            text = stringResource(R.string.rrr, details.currentRRR),
            modifier =
              Modifier.constrainAs(txtRrr) {
                end.linkTo(parent.end)
                top.linkTo(txtRequired.bottom, margin = 8.dp)
              },
          )
        } else {
          Button(
            onClick = endInning,
            modifier =
              Modifier.constrainAs(btnEndInning) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
              },
          ) {
            Text(text = stringResource(R.string.end_inning))
          }

          Text(
            text = stringResource(R.string.crr, details.currentCRR),
            modifier =
              Modifier.constrainAs(txtCrr) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(btnEndInning.bottom, margin = 16.dp)
              },
          )
        }

        Text(
          text = details.currentRunsAndWickets,
          modifier =
            Modifier.constrainAs(txtRuns) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              top.linkTo(txtCrr.bottom, margin = 16.dp)
            },
        )
        Text(
          text = details.currentOvers,
          modifier =
            Modifier.constrainAs(txtOvers) {
              start.linkTo(parent.start)
              top.linkTo(txtRuns.bottom, margin = 8.dp)
            },
        )
        IconButton(
          onClick = { /* open overs edit dialog */ },
          modifier =
            Modifier.constrainAs(btnEditOvers) {
              start.linkTo(txtOvers.end, margin = 8.dp)
              top.linkTo(txtOvers.top)
            },
        ) {
          Icon(painterResource(R.drawable.ic_edit), null)
        }

        // Wickets row
        Text(
          text = stringResource(R.string.wickets),
          modifier =
            Modifier.constrainAs(labelWickets) {
              start.linkTo(btnMinusWicket.end, margin = 8.dp)
              end.linkTo(btnAddWicket.start, margin = 8.dp)
              top.linkTo(txtOvers.bottom, margin = 16.dp)
            },
        )
        ExtendedFloatingActionButton(
          onClick = { setWicket(false) },
          modifier =
            Modifier.constrainAs(btnMinusWicket) {
              top.linkTo(labelWickets.top)
              bottom.linkTo(labelWickets.bottom)
              start.linkTo(parent.start)
            },
          icon = { Icon(painterResource(R.drawable.ic_minus), contentDescription = null) },
          text = {},
        )
        ExtendedFloatingActionButton(
          onClick = { setWicket(true) },
          modifier =
            Modifier.constrainAs(btnAddWicket) {
              top.linkTo(labelWickets.top)
              bottom.linkTo(labelWickets.bottom)
              end.linkTo(parent.end)
            },
          icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
          text = {},
        )

        // Overs buttons
        Text(
          text = stringResource(R.string.overs),
          modifier =
            Modifier.constrainAs(labelOvers) {
              start.linkTo(btnMinusBall.end, margin = 8.dp)
              end.linkTo(btnAddBall.start, margin = 8.dp)
              top.linkTo(labelWickets.bottom, margin = 24.dp)
            },
        )
        ExtendedFloatingActionButton(
          onClick = { setBall(1, false) },
          modifier =
            Modifier.constrainAs(btnMinusBall) {
              top.linkTo(labelOvers.top)
              bottom.linkTo(btnAddBall.top, margin = 8.dp)
              start.linkTo(parent.start)
            },
          icon = { Icon(painterResource(R.drawable.ic_minus), contentDescription = null) },
          text = {},
        )
        ExtendedFloatingActionButton(
          onClick = { setBall(1, true) },
          modifier =
            Modifier.constrainAs(btnAddBall) {
              start.linkTo(parent.start)
              bottom.linkTo(parent.bottom)
            },
          icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
          text = {},
        )

        // Runs buttons
        Text(
          text = stringResource(R.string.runs),
          modifier =
            Modifier.constrainAs(labelRuns) {
              start.linkTo(btnMinusRun.end, margin = 8.dp)
              end.linkTo(btnAddRun.start, margin = 8.dp)
              top.linkTo(labelOvers.bottom, margin = 24.dp)
            },
        )
        ExtendedFloatingActionButton(
          onClick = { setRun(1, false) },
          modifier =
            Modifier.constrainAs(btnMinusRun) {
              top.linkTo(labelRuns.top)
              bottom.linkTo(btnAddRun.top, margin = 8.dp)
              end.linkTo(parent.end)
            },
          icon = { Icon(painterResource(R.drawable.ic_minus), contentDescription = null) },
          text = {},
        )
        ExtendedFloatingActionButton(
          onClick = { setRun(1, true) },
          modifier =
            Modifier.constrainAs(btnAddRun) {
              end.linkTo(parent.end)
              bottom.linkTo(parent.bottom)
            },
          icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
          text = {},
        )
      }
    }
  }
}
