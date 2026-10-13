@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.match_record

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.CelebrationKind
import io.github.raghavsatyadev.library.support.components.CelebrationOverlay
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.InningsProgressBar
import io.github.raghavsatyadev.library.support.components.sharedContent
import io.github.raghavsatyadev.library.support.components.sharedMatchContainer
import io.github.raghavsatyadev.library.support.components.teamScoreKey
import io.github.raghavsatyadev.library.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.isMatchCompleted
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.oversToBalls
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.AppTheme
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.add_ball
import scus.composeapp.generated.resources.add_run
import scus.composeapp.generated.resources.add_wicket
import scus.composeapp.generated.resources.cancel
import scus.composeapp.generated.resources.celebrate_runs
import scus.composeapp.generated.resources.celebrate_wicket
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
import scus.composeapp.generated.resources.warning_overs_below_bowled
import scus.composeapp.generated.resources.wickets

@Composable
fun MatchRecordScreen(
  matchRecordId: String,
  preview: io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord? = null,
  viewModel: MatchRecordScreenViewModel = koinViewModel(),
  onBack: () -> Unit = {},
  onMatchCompleted: () -> Unit = {},
) {
  var showResetDialog by remember { mutableStateOf(false) }
  var showEditOversDialog by remember { mutableStateOf(false) }

  // Synchronous, before the first frame, so the screen is complete while its card grows into it.
  remember(preview) { viewModel.showPreview(preview) }
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
    matchId = matchRecordId,
    modifier = Modifier.sharedMatchContainer(matchRecordId, fullScreen = true),
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

/**
 * A score or overs value swapping in: it slides a little and fades on the quick effects spring, so
 * a number read as data never overshoots. An undo ([retract]) only fades: taking a ball back is a
 * correction, not progress, so it moves less than the add. With motion reduced it only fades.
 */
private fun <T> AnimatedContentTransitionScope<T>.dataSwap(
  slide: FiniteAnimationSpec<IntOffset>,
  fade: FiniteAnimationSpec<Float>,
  animate: Boolean,
  retract: Boolean,
): ContentTransform =
  if (animate && !retract) {
    (slideInVertically(slide) { it / 2 } + fadeIn(fade)) togetherWith
      (slideOutVertically(slide) { -it / 2 } + fadeOut(fade)) using
      SizeTransform(clip = false)
  } else {
    fadeIn(fade) togetherWith fadeOut(fade)
  }

/** Runs and wickets from a "72-5" score; null when it cannot be read. */
private fun parseScore(runsAndWickets: String): Pair<Int, Int>? {
  val parts = runsAndWickets.split('-')
  val runs = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
  val wickets = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
  return runs to wickets
}

/** How much of the innings is bowled, 0 to 1; 0 when the overs cannot be read. */
private fun inningsProgress(record: BasicMatchUIDetails): Float = runCatching {
  val total = oversToBalls(record.totalOvers)
  if (total <= 0) 0f else oversToBalls(record.currentOvers).toFloat() / total
}
  .getOrDefault(0f)

/** True in a chase whose required rate is above the current rate. */
private fun isBehind(record: BasicMatchUIDetails): Boolean {
  if (!record.isFirstInningComplete) return false
  val rrr = record.currentRRR.toDoubleOrNull() ?: return false
  val crr = record.currentCRR.toDoubleOrNull() ?: return false
  return rrr > crr
}

@Composable
private fun MatchRecordUI(
  record: BasicMatchUIDetails?,
  matchId: String = "",
  onBack: () -> Unit,
  endMatch: () -> Unit,
  endInning: () -> Unit,
  setWicket: (shouldAdd: Boolean) -> Unit,
  setBall: (balls: Int, shouldAdd: Boolean) -> Unit,
  setRun: (runs: Int, shouldAdd: Boolean) -> Unit,
  showResetDialog: () -> Unit,
  showEditOversDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val slide = AppMotion.tapDataSpec<IntOffset>()
  val fade = AppMotion.tapDataSpec<Float>()
  val animate = animationsEnabled()
  // The record arrives a moment after the screen opens; it fades in rather than popping in while
  // the card is still growing into the screen.
  val contentSpec = AppMotion.effectsSpec<Float>()
  val contentAlpha = remember { Animatable(if (record != null) 1f else 0f) }
  LaunchedEffect(record != null) { if (record != null) contentAlpha.animateTo(1f, contentSpec) }
  val haptic = LocalHapticFeedback.current
  // The direction of the last change, read when the score swaps: an undo resolves plainer.
  var lastWasUndo by remember { mutableStateOf(false) }
  // Adding is a light tick; taking back is a reject buzz, so a wrong tap feels different.
  val add = { action: () -> Unit ->
    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    lastWasUndo = false
    action()
  }
  val undo = { action: () -> Unit ->
    haptic.performHapticFeedback(HapticFeedbackType.Reject)
    lastWasUndo = true
    action()
  }
  // Celebrate events, not taps: a wicket gets a burst, every 50 runs a confetti shower. A run is
  // tapped a hundred times a match, so it only gets the score swap. Never on an undo or on open.
  var celebrationTrigger by remember { mutableIntStateOf(0) }
  var celebrationKind by remember { mutableStateOf(CelebrationKind.Wicket) }
  var milestoneRuns by remember { mutableIntStateOf(0) }
  val score = record?.let { parseScore(it.currentRunsAndWickets) }
  var previousScore by remember { mutableStateOf(score) }
  LaunchedEffect(score) {
    val before = previousScore
    previousScore = score
    if (before == null || score == null || lastWasUndo) return@LaunchedEffect
    val milestone = score.first / RUN_MILESTONE > before.first / RUN_MILESTONE
    val wicket = score.second > before.second
    if (milestone || wicket) {
      // A milestone outranks a wicket that lands on the same change.
      celebrationKind = if (milestone) CelebrationKind.Milestone else CelebrationKind.Wicket
      milestoneRuns = score.first / RUN_MILESTONE * RUN_MILESTONE
      celebrationTrigger++
      haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }
  }
  // Where the score sits, so the celebration bursts out of it.
  var scoreCenter by remember { mutableStateOf(Offset.Zero) }
  var overlayOrigin by remember { mutableStateOf(Offset.Zero) }

  Scaffold(
    modifier = modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      val title =
        record?.let { stringResource(Res.string.team) + " " + it.currentTeamName }
          ?: stringResource(Res.string.match_record_title)
      AppToolBar(
        title = title,
        onNavigateBack = onBack,
        actions = {
          IconButton(onClick = showResetDialog, shapes = IconButtonDefaults.shapes()) {
            Icon(
              painter = painterResource(Res.drawable.ic_refresh),
              contentDescription = stringResource(Res.string.reset_match),
            )
          }
        },
      )
    },
  ) { padding ->
    if (record == null) {
      // The record loads in a few milliseconds; the indicator only shows when it takes longer, so
      // it never flashes during the card's transform.
      var slow by remember { mutableStateOf(false) }
      LaunchedEffect(Unit) {
        delay(LOADING_INDICATOR_DELAY_MILLIS)
        slow = true
      }
      Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        if (slow) LoadingIndicator()
      }
      return@Scaffold
    }
    Box(
      modifier = Modifier.fillMaxSize().onGloballyPositioned { overlayOrigin = it.positionInRoot() }
    ) {
      ConstraintLayout(
        modifier =
          Modifier.fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .graphicsLayer { alpha = contentAlpha.value }
            .verticalScroll(rememberScrollState())
      ) {
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
          barProgress,
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
          shapes = ButtonDefaults.shapes(),
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
          color = MaterialTheme.colorScheme.outlineVariant,
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
          color = MaterialTheme.colorScheme.outlineVariant,
        )

        AnimatedContent(
          targetState = record.currentRunsAndWickets,
          modifier =
            Modifier.constrainAs(txtRunsWickets) {
                centerHorizontallyTo(parent)
                bottom.linkTo(txtOvers.top, 12.dp)
              }
              .onGloballyPositioned { scoreCenter = it.boundsInRoot().center }
              .sharedContent(teamScoreKey(matchId, record.isTeam1)),
          transitionSpec = { dataSwap(slide, fade, animate, retract = lastWasUndo) },
          label = "runs",
        ) {
          Text(
            text = it,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
          )
        }
        AnimatedContent(
          targetState = record.currentFormattedOvers,
          modifier =
            Modifier.constrainAs(txtOvers) {
              centerHorizontallyTo(parent)
              bottom.linkTo(barProgress.top, 12.dp)
            },
          transitionSpec = { dataSwap(slide, fade, animate, retract = lastWasUndo) },
          label = "overs",
        ) {
          Text(
            text = it,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
          )
        }

        if (!isFirstInningComplete) {
          IconButton(
            onClick = showEditOversDialog,
            shapes = IconButtonDefaults.shapes(),
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

        InningsProgressBar(
          progress = inningsProgress(record),
          behind = isBehind(record),
          modifier =
            Modifier.constrainAs(barProgress) {
              start.linkTo(parent.start)
              end.linkTo(parent.end)
              bottom.linkTo(btnAddWicket.top, 16.dp)
              width = Dimension.fillToConstraints
            },
        )

        FloatingActionButton(
          onClick = { add { setWicket(true) } },
          containerColor = MaterialTheme.colorScheme.tertiaryContainer,
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
          onClick = { undo { setWicket(false) } },
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
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
          color = MaterialTheme.colorScheme.outlineVariant,
        )

        FloatingActionButton(
          onClick = { undo { setRun(1, false) } },
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
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
          onClick = { undo { setBall(1, false) } },
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
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

        LargeFloatingActionButton(
          onClick = { add { setRun(1, true) } },
          modifier =
            Modifier.constrainAs(btnAddRun) {
              centerHorizontallyTo(parent)
              top.linkTo(guidelineAddButtons)
              bottom.linkTo(parent.bottom)
            },
        ) {
          Icon(
            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.add_run),
          )
        }
        LargeFloatingActionButton(
          onClick = { add { setBall(1, true) } },
          modifier =
            Modifier.constrainAs(btnAddBall) {
              centerHorizontallyTo(parent)
              top.linkTo(guidelineAddButtons)
              bottom.linkTo(parent.bottom)
            },
        ) {
          Icon(
            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.add_ball),
          )
        }
      }
      // Over everything, but with no pointer input: taps reach the scoring buttons beneath it.
      CelebrationOverlay(
        trigger = celebrationTrigger,
        kind = celebrationKind,
        label =
          if (celebrationKind == CelebrationKind.Milestone) {
            stringResource(Res.string.celebrate_runs, milestoneRuns)
          } else {
            stringResource(Res.string.celebrate_wicket)
          },
        origin = scoreCenter - overlayOrigin,
        colors =
          listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.error,
          ),
        modifier = Modifier.fillMaxSize(),
      )
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
          shapes = ButtonDefaults.shapes(),
          onClick = {
            onReset(false)
            dismissDialog()
          },
        ) {
          Text(stringResource(Res.string.reset_inning))
        }
        if (record.isFirstInningComplete) {
          TextButton(
            shapes = ButtonDefaults.shapes(),
            onClick = {
              onReset(true)
              dismissDialog()
            },
          ) {
            Text(stringResource(Res.string.reset_full))
          }
        }
      }
    },
    dismissButton = {
      TextButton(
        shapes = ButtonDefaults.shapes(),
        onClick = dismissDialog,
      ) {
        Text(stringResource(Res.string.cancel))
      }
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
  var belowBowled by remember { mutableStateOf(false) }
  AlertDialog(
    onDismissRequest = dismissDialog,
    title = { Text(stringResource(Res.string.edit_total_overs)) },
    text = {
      OutlinedTextField(
        value = editedOvers,
        onValueChange = {
          editedOvers = it
          belowBowled = false
        },
        singleLine = true,
        isError = belowBowled,
        supportingText =
          if (belowBowled) {
            { Text(stringResource(Res.string.warning_overs_below_bowled)) }
          } else {
            null
          },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
    },
    confirmButton = {
      TextButton(
        shapes = ButtonDefaults.shapes(),
        onClick = {
          try {
            val editedBalls = oversToBalls(editedOvers)
            if (editedBalls < oversToBalls(record.currentFormattedOvers)) {
              belowBowled = true
              return@TextButton
            }
            editedOversInBalls(editedBalls)
          } catch (_: Exception) {}
        },
      ) {
        Text(stringResource(Res.string.save))
      }
    },
    dismissButton = {
      TextButton(
        shapes = ButtonDefaults.shapes(),
        onClick = dismissDialog,
      ) {
        Text(stringResource(Res.string.cancel))
      }
    },
  )
}

private const val LOADING_INDICATOR_DELAY_MILLIS = 400L

/** A run milestone worth a bigger celebration: 50, 100, 150... */
private const val RUN_MILESTONE = 50
