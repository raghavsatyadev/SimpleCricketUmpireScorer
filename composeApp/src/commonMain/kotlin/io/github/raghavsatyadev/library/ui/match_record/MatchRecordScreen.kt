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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
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
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.CelebrationKind
import io.github.raghavsatyadev.library.support.components.CelebrationOverlay
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.InningsProgressBar
import io.github.raghavsatyadev.library.support.components.StatusPill
import io.github.raghavsatyadev.library.support.components.StatusTone
import io.github.raghavsatyadev.library.support.components.matchLocationKey
import io.github.raghavsatyadev.library.support.components.matchStatusKey
import io.github.raghavsatyadev.library.support.components.matchTimingsKey
import io.github.raghavsatyadev.library.support.components.sharedContent
import io.github.raghavsatyadev.library.support.components.sharedMatchContainer
import io.github.raghavsatyadev.library.support.components.teamNameKey
import io.github.raghavsatyadev.library.support.components.teamOversKey
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
import scus.composeapp.generated.resources.batting
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
import scus.composeapp.generated.resources.in_progress
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
          isTeam1 = false,
          otherTeamName = "Raghav",
          otherTeamRunsAndWickets = "40-3",
          otherTeamOvers = "(6.0)",
          location = "Ahmedabad",
          matchTimings = "22 Apr 2025, 11:44",
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
      Column(
        modifier =
          Modifier.fillMaxSize().padding(padding).padding(16.dp).graphicsLayer {
            alpha = contentAlpha.value
          }
      ) {
        // The match details take what is left above the score and scroll only when they do not fit
        // (a short screen, a large font); the score and the scoring buttons never scroll away.
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
          Column(
            modifier =
              Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            // The header at the top, the rates just above the score.
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            MatchHeader(record = record, matchId = matchId, modifier = Modifier.fillMaxWidth())
            MatchRates(
              record = record,
              endInning = endInning,
              endMatch = endMatch,
              modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
          }
        }
        MatchScore(
          record = record,
          matchId = matchId,
          lastWasUndo = lastWasUndo,
          slide = slide,
          fade = fade,
          animate = animate,
          showEditOversDialog = showEditOversDialog,
          onScorePositioned = { scoreCenter = it },
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        ScoringButtons(
          add = add,
          undo = undo,
          setWicket = setWicket,
          setBall = setBall,
          setRun = setRun,
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
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

/**
 * The end-innings action and the rates: the current rate, and in a chase the target and the rate it
 * needs, side by side to keep the score and the buttons below them in view.
 */
@Composable
private fun MatchRates(
  record: BasicMatchUIDetails,
  endInning: () -> Unit,
  endMatch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isFirstInningComplete = record.isFirstInningComplete
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Button(
      shapes = ButtonDefaults.shapes(),
      onClick = if (isFirstInningComplete) endMatch else endInning,
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
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val rateModifier = if (isFirstInningComplete) Modifier.weight(1f) else Modifier
      Text(
        text = stringResource(Res.string.crr, record.currentCRR),
        style = MaterialTheme.typography.titleMedium,
        modifier = rateModifier,
      )
      if (isFirstInningComplete) {
        Text(
          text = stringResource(Res.string.required_runs_balls, record.requiredRunsBalls),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          modifier = Modifier.weight(1f),
        )
        Text(
          text = stringResource(Res.string.rrr, record.currentRRR),
          style = MaterialTheme.typography.titleMedium,
          textAlign = TextAlign.End,
          modifier = Modifier.weight(1f),
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
  }
}

/**
 * The score, the overs and how much of the innings is bowled, kept in view above the scoring
 * buttons. The score and overs share keys with the dashboard card, so they fly into place as the
 * screen grows out of it.
 */
@Composable
private fun MatchScore(
  record: BasicMatchUIDetails,
  matchId: String,
  lastWasUndo: Boolean,
  slide: FiniteAnimationSpec<IntOffset>,
  fade: FiniteAnimationSpec<Float>,
  animate: Boolean,
  showEditOversDialog: () -> Unit,
  onScorePositioned: (Offset) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isFirstInningComplete = record.isFirstInningComplete
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    AnimatedContent(
      targetState = record.currentRunsAndWickets,
      modifier =
        Modifier.onGloballyPositioned { onScorePositioned(it.boundsInRoot().center) }
          .sharedContent(teamScoreKey(matchId, record.isTeam1)),
      transitionSpec = { dataSwap(slide, fade, animate, retract = lastWasUndo) },
      label = "runs",
    ) {
      Text(text = it, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Balances the edit button, so the overs stay centred under the score.
      if (!isFirstInningComplete) Spacer(modifier = Modifier.size(EDIT_BUTTON_SIZE))
      AnimatedContent(
        targetState = record.currentFormattedOvers,
        modifier = Modifier.sharedContent(teamOversKey(matchId, record.isTeam1)),
        transitionSpec = { dataSwap(slide, fade, animate, retract = lastWasUndo) },
        label = "overs",
      ) {
        Text(text = it, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
      }
      if (!isFirstInningComplete) {
        IconButton(
          onClick = showEditOversDialog,
          shapes = IconButtonDefaults.shapes(),
          modifier = Modifier.size(EDIT_BUTTON_SIZE),
        ) {
          Icon(
            tint = MaterialTheme.colorScheme.primary,
            painter = painterResource(Res.drawable.ic_edit),
            contentDescription = stringResource(Res.string.edit_total_overs),
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    InningsProgressBar(
      progress = inningsProgress(record),
      behind = isBehind(record),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

/**
 * The scoring buttons, pinned to the bottom of the screen so the umpire can always reach them:
 * wickets on top, then the undo row, then the two big add buttons for balls and runs.
 */
@Composable
private fun ScoringButtons(
  add: (() -> Unit) -> Unit,
  undo: (() -> Unit) -> Unit,
  setWicket: (shouldAdd: Boolean) -> Unit,
  setBall: (balls: Int, shouldAdd: Boolean) -> Unit,
  setRun: (runs: Int, shouldAdd: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  ConstraintLayout(modifier = modifier) {
    val (labelOvers, labelRuns, labelWickets, spaceMinusTop) = createRefs()
    val (btnMinusBall, btnAddBall, btnMinusRun, btnAddRun, btnMinusWicket, btnAddWicket) =
      createRefs()

    createHorizontalChain(btnAddBall, btnAddRun)

    FloatingActionButton(
      onClick = { add { setWicket(true) } },
      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
      modifier =
        Modifier.constrainAs(btnAddWicket) {
          centerHorizontallyTo(btnAddRun)
          top.linkTo(parent.top)
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
          top.linkTo(parent.top)
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
          bottom.linkTo(btnAddRun.top)
        },
      textAlign = TextAlign.Center,
      text = stringResource(Res.string.runs),
      style = MaterialTheme.typography.titleLarge,
    )
    Text(
      modifier =
        Modifier.constrainAs(labelOvers) {
          centerHorizontallyTo(btnAddBall)
          bottom.linkTo(btnAddBall.top)
        },
      textAlign = TextAlign.Center,
      text = stringResource(Res.string.overs),
      style = MaterialTheme.typography.titleLarge,
    )

    LargeFloatingActionButton(
      onClick = { add { setRun(1, true) } },
      modifier = Modifier.constrainAs(btnAddRun) { bottom.linkTo(parent.bottom) },
    ) {
      Icon(
        modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
        painter = painterResource(Res.drawable.ic_add),
        contentDescription = stringResource(Res.string.add_run),
      )
    }
    LargeFloatingActionButton(
      onClick = { add { setBall(1, true) } },
      modifier = Modifier.constrainAs(btnAddBall) { bottom.linkTo(parent.bottom) },
    ) {
      Icon(
        modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
        painter = painterResource(Res.drawable.ic_add),
        contentDescription = stringResource(Res.string.add_ball),
      )
    }
  }
}

/**
 * The match card, kept at the top of the scoring screen: its status, both teams, and where and when
 * the match is played. Each piece shares a key with its twin on the dashboard card, so the card's
 * pieces fly into place as the screen grows out of it. Team 1 stays on the left, as on the card;
 * the batting team, whose score is the big one below, is marked instead of scored here.
 */
@Composable
private fun MatchHeader(
  record: BasicMatchUIDetails,
  matchId: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    StatusPill(
      text = stringResource(Res.string.in_progress),
      tone = StatusTone.Active,
      modifier = Modifier.sharedContent(matchStatusKey(matchId)),
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
      listOf(true, false).forEach { team1 ->
        val batting = team1 == record.isTeam1
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = if (batting) record.currentTeamName else record.otherTeamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color =
              if (batting) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.sharedContent(teamNameKey(matchId, team1)),
          )
          if (batting) {
            Text(
              text = stringResource(Res.string.batting),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
            )
          } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = record.otherTeamRunsAndWickets,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.sharedContent(teamScoreKey(matchId, team1)),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = record.otherTeamOvers,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.sharedContent(teamOversKey(matchId, team1)),
              )
            }
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    // On one line when it fits; the timings wrap whole onto a second line when it does not.
    FlowRow(
      horizontalArrangement = Arrangement.Center,
      itemVerticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = record.location,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.sharedContent(matchLocationKey(matchId)),
      )
      if (record.location.isNotBlank()) {
        Text(text = " · ", style = MaterialTheme.typography.bodyMedium)
      }
      Text(
        text = record.matchTimings,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.sharedContent(matchTimingsKey(matchId)),
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

/** The edit-overs button beside the overs, and the space balancing it on the other side. */
private val EDIT_BUTTON_SIZE = 40.dp

/** A run milestone worth a bigger celebration: 50, 100, 150... */
private const val RUN_MILESTONE = 50
