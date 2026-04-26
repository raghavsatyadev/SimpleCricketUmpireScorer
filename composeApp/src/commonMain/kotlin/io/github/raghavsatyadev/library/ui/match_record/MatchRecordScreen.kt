@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.match_record

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.oversToBalls
import io.github.raghavsatyadev.library.support.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.add_ball
import scus.composeapp.generated.resources.add_run
import scus.composeapp.generated.resources.add_wicket
import scus.composeapp.generated.resources.cancel
import scus.composeapp.generated.resources.crr
import scus.composeapp.generated.resources.edit_total_overs
import scus.composeapp.generated.resources.end_inning
import scus.composeapp.generated.resources.end_match
import scus.composeapp.generated.resources.ic_add
import scus.composeapp.generated.resources.ic_edit
import scus.composeapp.generated.resources.ic_minus
import scus.composeapp.generated.resources.ic_refresh
import scus.composeapp.generated.resources.match_record_title
import scus.composeapp.generated.resources.minus_ball
import scus.composeapp.generated.resources.minus_run
import scus.composeapp.generated.resources.minus_wicket
import scus.composeapp.generated.resources.overs
import scus.composeapp.generated.resources.required_runs_balls
import scus.composeapp.generated.resources.reset_full
import scus.composeapp.generated.resources.reset_full_match_message
import scus.composeapp.generated.resources.reset_inning
import scus.composeapp.generated.resources.reset_inning_message
import scus.composeapp.generated.resources.reset_match
import scus.composeapp.generated.resources.rrr
import scus.composeapp.generated.resources.runs
import scus.composeapp.generated.resources.save
import scus.composeapp.generated.resources.team
import scus.composeapp.generated.resources.wickets

@Composable
fun MatchRecordScreen(
  matchRecordId: String,
  viewModel: MatchRecordScreenViewModel = koinViewModel(),
  onBack: () -> Unit = {},
  onMatchCompleted: () -> Unit = {},
) {
  var showResetDialog by remember { mutableStateOf(false) }
  var showEditOversDialog by remember { mutableStateOf(false) }

  LaunchedEffect(matchRecordId) { viewModel.loadMatchRecord(matchRecordId) }

  DisposableEffect(Unit) { onDispose { viewModel.clearLoadingState() } }

  val recordState by viewModel.matchRecordEvent.collectAsState()
  val matchStatus = recordState?.matchStatus

  LaunchedEffect(matchStatus) {
    if (recordState?.isMatchCompleted() == true) {
      onMatchCompleted()
    }
  }

  recordState?.let { record ->
    if (showResetDialog) {
      ResetDialog(
        record,
        onReset = {
          viewModel.reset(matchRecordId, it)
          showResetDialog = false
        },
        dismissDialog = { showResetDialog = false },
      )
    }

    if (showEditOversDialog) {
      EditOversDialog(
        record,
        editedOversInBalls = {
          viewModel.editTotalOvers(matchRecordId, it)
          showEditOversDialog = false
        },
        dismissDialog = { showEditOversDialog = false },
      )
    }
  }
  MatchRecordUI(
    record = recordState,
    onBack = onBack,
    endInning = { viewModel.endInning(matchRecordId) },
    endMatch = { viewModel.endMatch(matchRecordId) },
    setWicket = { viewModel.setWicket(matchRecordId, it) },
    setBall = { balls, isAdd -> viewModel.setBall(matchRecordId, balls, isAdd) },
    setRun = { runs, isAdd -> viewModel.setRun(matchRecordId, runs, isAdd) },
    showResetDialog = { showResetDialog = true },
    showEditOversDialog = { showEditOversDialog = true },
  )
}

@DarkRealDevicePreview
@Composable
fun MatchRecordUIPreview() {
  AppTheme {
    MatchRecordUI(
      record =
        BasicMatchUIDetails(
          currentTeamName = "Archan",
          currentRunsAndWickets = "36-6",
          currentFormattedOvers = "3.3 / 6.0",
          totalOvers = "10",
          currentCRR = "7.3",
          currentRRR = "8.3",
          requiredRunsBalls = "5 (36)",
          currentOvers = "3.3",
        ),
      onBack = {},
      endMatch = {},
      endInning = {},
      setWicket = {},
      setBall = { _, _ -> },
      setRun = { _, _ -> },
      showResetDialog = {},
      showEditOversDialog = {},
    )
  }
}

@Composable
private fun MatchRecordUI(
  record: BasicMatchUIDetails?,
  onBack: () -> Unit,
  endMatch: () -> Unit,
  endInning: () -> Unit,
  setWicket: (shouldAdd: Boolean) -> Unit,
  setBall: (balls: Int, shouldAdd: Boolean) -> Unit,
  setRun: (runs: Int, shouldAdd: Boolean) -> Unit,
  showResetDialog: () -> Unit,
  showEditOversDialog: () -> Unit,
) {

  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      val title =
        record?.let { stringResource(Res.string.team) + " " + it.currentTeamName }
          ?: stringResource(Res.string.match_record_title)
      AppToolBar(
        title = title,
        onNavigateBack = onBack,
        actions = {
          IconButton(onClick = showResetDialog) {
            Icon(
              painter = painterResource(Res.drawable.ic_refresh),
              contentDescription = stringResource(Res.string.reset_match),
            )
          }
        },
      )
    },
  ) { padding ->
    ConstraintLayout(
      modifier =
        Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
      if (record == null) return@ConstraintLayout
      val (
        txtRequired,
        txtCrr,
        txtRrr,
        txtRunsWickets,
        txtOvers,
        labelOvers,
        labelRuns,
        labelWickets,
        spaceRr,
        spaceRunsWickets,
        spaceMinusTop,
      ) = createRefs()

      val (
        btnEndInningMatch,
        btnEditOvers,
        btnMinusBall,
        btnAddBall,
        btnMinusRun,
        btnAddRun,
        btnMinusWicket,
        btnAddWicket,
      ) = createRefs()

      createHorizontalChain(btnAddBall, btnAddRun)

      val isFirstInningComplete = record.isFirstInningComplete

      if (isFirstInningComplete) createHorizontalChain(txtCrr, txtRrr)

      val guidelineAddButtons = createGuidelineFromTop(0.8f)

      Button(
        onClick = {
          if (isFirstInningComplete) {
            endMatch()
          } else {
            endInning()
          }
        },
        modifier =
          Modifier.constrainAs(btnEndInningMatch) {
            centerHorizontallyTo(parent)
            bottom.linkTo(if (isFirstInningComplete) txtRequired.top else spaceRr.top, 12.dp)
          },
      ) {
        Text(
          text =
            if (isFirstInningComplete) {
              stringResource(Res.string.end_match)
            } else {
              stringResource(Res.string.end_inning)
            }
        )
      }

      if (isFirstInningComplete) {
        Text(
          text = stringResource(Res.string.required_runs_balls, record.requiredRunsBalls),
          style = MaterialTheme.typography.titleLarge,
          textAlign = TextAlign.Center,
          modifier =
            Modifier.constrainAs(txtRequired) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              bottom.linkTo(spaceRr.top, 8.dp)
            },
        )
      }
      HorizontalDivider(
        modifier =
          Modifier.constrainAs(spaceRr) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(txtCrr.top, 8.dp)
          },
        thickness = 1.dp,
      )

      Text(
        text = stringResource(Res.string.crr, record.currentCRR),
        style = MaterialTheme.typography.titleLarge,
        modifier =
          Modifier.constrainAs(txtCrr) {
            if (isFirstInningComplete) {
              start.linkTo(parent.start)
            } else {
              centerHorizontallyTo(parent)
            }
            bottom.linkTo(spaceRunsWickets.top, 8.dp)
          },
      )
      if (isFirstInningComplete) {
        Text(
          text = stringResource(Res.string.rrr, record.currentRRR),
          style = MaterialTheme.typography.titleLarge,
          modifier =
            Modifier.constrainAs(txtRrr) {
              end.linkTo(parent.end)
              bottom.linkTo(spaceRunsWickets.top, 8.dp)
            },
        )
      }

      HorizontalDivider(
        modifier =
          Modifier.constrainAs(spaceRunsWickets) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(txtRunsWickets.top, 8.dp)
          },
        thickness = 1.dp,
      )

      Text(
        text = record.currentRunsAndWickets,
        style = MaterialTheme.typography.displayMedium,
        modifier =
          Modifier.constrainAs(txtRunsWickets) {
            centerHorizontallyTo(parent)
            bottom.linkTo(txtOvers.top, 12.dp)
          },
      )
      Text(
        text = record.currentFormattedOvers,
        style = MaterialTheme.typography.displaySmall,
        modifier =
          Modifier.constrainAs(txtOvers) {
            centerHorizontallyTo(parent)
            bottom.linkTo(btnAddWicket.top, 12.dp)
          },
      )

      if (!isFirstInningComplete) {
        IconButton(
          onClick = showEditOversDialog,
          modifier =
            Modifier.constrainAs(btnEditOvers) {
              start.linkTo(txtOvers.end)
              centerVerticallyTo(txtOvers)
            },
        ) {
          Icon(
            tint = MaterialTheme.colorScheme.primary,
            painter = painterResource(Res.drawable.ic_edit),
            contentDescription = stringResource(Res.string.edit_total_overs),
          )
        }
      }

      FloatingActionButton(
        onClick = { setWicket(true) },
        modifier =
          Modifier.constrainAs(btnAddWicket) {
            centerHorizontallyTo(btnAddRun)
            bottom.linkTo(spaceMinusTop.top, 12.dp)
          },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_add),
          contentDescription = stringResource(Res.string.add_wicket),
        )
      }
      Text(
        modifier =
          Modifier.constrainAs(labelWickets) {
            centerHorizontallyTo(parent)
            centerVerticallyTo(btnAddWicket)
          },
        textAlign = TextAlign.Center,
        text = stringResource(Res.string.wickets),
        style = MaterialTheme.typography.titleLarge,
      )
      FloatingActionButton(
        onClick = { setWicket(false) },
        modifier =
          Modifier.constrainAs(btnMinusWicket) {
            centerHorizontallyTo(btnAddBall)
            bottom.linkTo(spaceMinusTop.top, 12.dp)
          },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_minus),
          contentDescription = stringResource(Res.string.minus_wicket),
        )
      }

      HorizontalDivider(
        modifier =
          Modifier.constrainAs(spaceMinusTop) {
            centerHorizontallyTo(parent)
            bottom.linkTo(btnMinusRun.top, 12.dp)
          },
        thickness = 1.dp,
      )

      FloatingActionButton(
        onClick = { setRun(1, false) },
        modifier =
          Modifier.constrainAs(btnMinusRun) {
            centerHorizontallyTo(btnAddRun)
            bottom.linkTo(labelRuns.top, 12.dp)
          },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_minus),
          contentDescription = stringResource(Res.string.minus_run),
        )
      }
      FloatingActionButton(
        onClick = { setBall(1, false) },
        modifier =
          Modifier.constrainAs(btnMinusBall) {
            centerHorizontallyTo(btnAddBall)
            bottom.linkTo(labelOvers.top, 12.dp)
          },
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_minus),
          contentDescription = stringResource(Res.string.minus_ball),
        )
      }
      Text(
        modifier =
          Modifier.constrainAs(labelRuns) {
            centerHorizontallyTo(btnAddRun)
            bottom.linkTo(guidelineAddButtons)
          },
        textAlign = TextAlign.Center,
        text = stringResource(Res.string.runs),
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        modifier =
          Modifier.constrainAs(labelOvers) {
            centerHorizontallyTo(btnAddBall)
            bottom.linkTo(guidelineAddButtons)
          },
        textAlign = TextAlign.Center,
        text = stringResource(Res.string.overs),
        style = MaterialTheme.typography.titleLarge,
      )

      FloatingActionButton(
        onClick = { setRun(1, true) },
        modifier =
          Modifier.size(100.dp).constrainAs(btnAddRun) {
            centerHorizontallyTo(parent)
            top.linkTo(guidelineAddButtons)
            bottom.linkTo(parent.bottom)
          },
      ) {
        Icon(
          modifier = Modifier.size(50.dp),
          painter = painterResource(Res.drawable.ic_add),
          contentDescription = stringResource(Res.string.add_run),
        )
      }
      FloatingActionButton(
        onClick = { setBall(1, true) },
        modifier =
          Modifier.size(100.dp).constrainAs(btnAddBall) {
            centerHorizontallyTo(parent)
            top.linkTo(guidelineAddButtons)
            bottom.linkTo(parent.bottom)
          },
      ) {
        Icon(
          modifier = Modifier.size(50.dp),
          painter = painterResource(Res.drawable.ic_add),
          contentDescription = stringResource(Res.string.add_ball),
        )
      }
    }
  }
}

@Composable
private fun ResetDialog(
  record: BasicMatchUIDetails,
  onReset: (Boolean) -> Unit,
  dismissDialog: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = dismissDialog,
    title = { Text(stringResource(Res.string.reset_match)) },
    text = {
      Text(
        if (record.isFirstInningComplete) {
          stringResource(Res.string.reset_full_match_message)
        } else {
          stringResource(Res.string.reset_inning_message)
        }
      )
    },
    confirmButton = {
      Row {
        TextButton(
          onClick = {
            onReset(false)
            dismissDialog()
          }
        ) {
          Text(stringResource(Res.string.reset_inning))
        }
        if (record.isFirstInningComplete) {
          TextButton(
            onClick = {
              onReset(true)
              dismissDialog()
            }
          ) {
            Text(stringResource(Res.string.reset_full))
          }
        }
      }
    },
    dismissButton = {
      TextButton(onClick = dismissDialog) { Text(stringResource(Res.string.cancel)) }
    },
  )
}

@Composable
private fun EditOversDialog(
  record: BasicMatchUIDetails,
  editedOversInBalls: (Int) -> Unit,
  dismissDialog: () -> Unit,
) {
  var editedOvers by remember { mutableStateOf(record.totalOvers) }
  AlertDialog(
    onDismissRequest = dismissDialog,
    title = { Text(stringResource(Res.string.edit_total_overs)) },
    text = {
      TextField(
        value = editedOvers,
        onValueChange = { editedOvers = it },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          try {
            val editedBalls = oversToBalls(editedOvers)
            if (editedBalls < oversToBalls(record.currentFormattedOvers)) {
              return@TextButton
            }
            editedOversInBalls(editedBalls)
          } catch (_: Exception) {}
        }
      ) {
        Text(stringResource(Res.string.save))
      }
    },
    dismissButton = {
      TextButton(onClick = dismissDialog) { Text(stringResource(Res.string.cancel)) }
    },
  )
}
