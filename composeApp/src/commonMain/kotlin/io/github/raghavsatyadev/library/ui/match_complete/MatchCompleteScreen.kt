@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.match_complete

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.DriftingShape
import io.github.raghavsatyadev.library.support.components.StatusPill
import io.github.raghavsatyadev.library.support.components.StatusTone
import io.github.raghavsatyadev.library.support.components.liquidRipple
import io.github.raghavsatyadev.library.support.components.rememberLiquidRippleState
import io.github.raghavsatyadev.library.support.components.sharedContent
import io.github.raghavsatyadev.library.support.components.sharedMatchContainer
import io.github.raghavsatyadev.library.support.components.teamNameKey
import io.github.raghavsatyadev.library.support.components.teamScoreKey
import io.github.raghavsatyadev.library.support.components.teamStatusKey
import io.github.raghavsatyadev.library.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import io.github.raghavsatyadev.library.support.models.db.match_record.TeamDetail
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.AppTheme
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.crr
import scus.composeapp.generated.resources.draw
import scus.composeapp.generated.resources.ic_check
import scus.composeapp.generated.resources.lost
import scus.composeapp.generated.resources.match_complete_title
import scus.composeapp.generated.resources.required_runs_balls_at_end
import scus.composeapp.generated.resources.rrr_at_start
import scus.composeapp.generated.resources.team_1
import scus.composeapp.generated.resources.team_2
import scus.composeapp.generated.resources.won

@Composable
fun MatchCompleteScreen(
  matchId: String,
  preview: MatchRecord? = null,
  viewModel: MatchCompleteScreenViewModel = koinViewModel(),
  onBack: () -> Unit = {},
) {
  // Synchronous, before the first frame, so the screen is complete while its card grows into it.
  remember(preview) { viewModel.showPreview(preview) }
  LaunchedEffect(matchId) { viewModel.loadMatchRecord(matchId) }

  val matchRecord by viewModel.matchRecord.collectAsState()

  val team1Details = remember(matchRecord) { matchRecord?.toBasicMatchUIDetails(true) }
  val team2Details = remember(matchRecord) { matchRecord?.toBasicMatchUIDetails(false) }

  MatchCompleteUI(
    onBack,
    matchRecord,
    team1Details,
    team2Details,
    Modifier.sharedMatchContainer(matchId, fullScreen = true),
  )
}

private fun <T> AnimatedContentTransitionScope<T>.verticalSwap(
  motion: MotionScheme
): ContentTransform =
  (slideInVertically(motion.fastSpatialSpec()) { it / 2 } +
    fadeIn(motion.fastEffectsSpec())) togetherWith
    (slideOutVertically(motion.fastSpatialSpec()) { -it / 2 } +
      fadeOut(motion.fastEffectsSpec())) using
    SizeTransform(clip = false)

/** For a value read as data, such as the score: the same swap, but it never overshoots. */
private fun <T> AnimatedContentTransitionScope<T>.dataSwap(
  slide: FiniteAnimationSpec<IntOffset>,
  fade: FiniteAnimationSpec<Float>,
): ContentTransform =
  (slideInVertically(slide) { it / 2 } + fadeIn(fade)) togetherWith
    (slideOutVertically(slide) { -it / 2 } + fadeOut(fade)) using
    SizeTransform(clip = false)

@Composable
private fun MatchCompleteUI(
  onBack: () -> Unit,
  matchRecord: MatchRecord?,
  team1Details: BasicMatchUIDetails?,
  team2Details: BasicMatchUIDetails?,
  modifier: Modifier = Modifier,
) {
  val motion = MaterialTheme.motionScheme
  var showTeam1 by
    remember(matchRecord) {
      mutableStateOf(
        matchRecord?.status == MatchStatus.TEAM_1_WON || matchRecord?.status == MatchStatus.DRAW
      )
    }
  val details: BasicMatchUIDetails? = if (showTeam1) team1Details else team2Details
  val matchId = matchRecord?.matchRecordId.orEmpty()
  val shownTeamWon =
    (showTeam1 && matchRecord?.status == MatchStatus.TEAM_1_WON) ||
      (!showTeam1 && matchRecord?.status == MatchStatus.TEAM_2_WON)
  val dataSlide = AppMotion.dataSpec<IntOffset>()
  val dataFade = AppMotion.dataSpec<Float>()
  val ripple = rememberLiquidRippleState()
  // The record arrives a moment after the screen opens; the result fades in rather than popping in
  // while the card is still growing into the screen.
  val contentSpec = AppMotion.effectsSpec<Float>()
  val contentAlpha = remember { Animatable(if (details != null) 1f else 0f) }
  LaunchedEffect(details != null) { if (details != null) contentAlpha.animateTo(1f, contentSpec) }

  Scaffold(
    modifier = modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      AppToolBar(title = stringResource(Res.string.match_complete_title), onNavigateBack = onBack)
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Main Content centered, with a slowly turning shape behind the winner's score
      // A tap on the result sends a ripple across it: pure delight, nothing to read.
      Box(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .graphicsLayer { alpha = contentAlpha.value }
            .liquidRipple(
              ripple,
              enabled = animationsEnabled(),
              highlight = MaterialTheme.colorScheme.primaryContainer,
            ),
        contentAlignment = Alignment.Center,
      ) {
        // Every result gets its hero shape, told apart by shape as well as colour: a sunny cookie
        // for a win, a soft puffy cloud for a loss, a clover for a draw.
        val shownTeamLost =
          (showTeam1 && matchRecord?.status == MatchStatus.TEAM_2_WON) ||
            (!showTeam1 && matchRecord?.status == MatchStatus.TEAM_1_WON)
        val hero =
          when {
            shownTeamWon ->
              MaterialShapes.Cookie9Sided to MaterialTheme.colorScheme.primaryContainer
            shownTeamLost ->
              MaterialShapes.Puffy to MaterialTheme.colorScheme.surfaceContainerHighest
            matchRecord?.status == MatchStatus.DRAW ->
              MaterialShapes.Clover8Leaf to MaterialTheme.colorScheme.tertiaryContainer
            else -> null
          }
        val heroFade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        AnimatedContent(
          targetState = hero,
          transitionSpec = { fadeIn(heroFade) togetherWith fadeOut(heroFade) },
          label = "hero",
        ) { shown ->
          if (shown != null) {
            DriftingShape(
              color = shown.second.copy(alpha = 0.6f),
              shape = shown.first,
              size = 300.dp,
            )
          }
        }
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          details?.let { d ->
            matchRecord?.status?.let { status ->
              val isWin =
                (showTeam1 && status == MatchStatus.TEAM_1_WON) ||
                  (!showTeam1 && status == MatchStatus.TEAM_2_WON)
              val isLoss =
                (showTeam1 && status == MatchStatus.TEAM_2_WON) ||
                  (!showTeam1 && status == MatchStatus.TEAM_1_WON)
              val isDraw = status == MatchStatus.DRAW

              val textRes =
                when {
                  isWin -> Res.string.won
                  isLoss -> Res.string.lost
                  isDraw -> Res.string.draw
                  else -> null
                }
              val tone =
                when {
                  isWin -> StatusTone.Positive
                  isLoss -> StatusTone.Negative
                  isDraw -> StatusTone.Neutral
                  else -> null
                }

              if (textRes != null && tone != null) {
                StatusPill(
                  text = stringResource(textRes).uppercase(),
                  tone = tone,
                  modifier = Modifier.sharedContent(teamStatusKey(matchId, showTeam1)),
                )
                Spacer(modifier = Modifier.height(8.dp))
              }
            }

            // Team Name
            AnimatedContent(
              targetState = d.currentTeamName,
              modifier = Modifier.sharedContent(teamNameKey(matchId, showTeam1)),
              transitionSpec = { verticalSwap(motion) },
              label = "teamName",
            ) { teamName ->
              Text(
                text = teamName,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            matchRecord?.let { record ->
              val isBattingFirst = record.isTeam1BattingFirst
              val showRequired = (showTeam1 && !isBattingFirst) || (!showTeam1 && isBattingFirst)

              if (showRequired) {
                Text(
                  text = stringResource(Res.string.required_runs_balls_at_end, d.requiredRunsBalls),
                  style = MaterialTheme.typography.titleLarge,
                  textAlign = TextAlign.Center,
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
              modifier = Modifier.fillMaxWidth(0.8f),
              color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            matchRecord?.let { record ->
              val isBattingFirst = record.isTeam1BattingFirst
              val showRequired = (showTeam1 && !isBattingFirst) || (!showTeam1 && isBattingFirst)

              if (showRequired) {
                Text(
                  text = stringResource(Res.string.rrr_at_start, record.rrrAtSecondInningStart),
                  style = MaterialTheme.typography.titleMedium,
                  textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
              }
              Text(
                text = stringResource(Res.string.crr, d.currentCRR),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
              modifier = Modifier.fillMaxWidth(0.8f),
              color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Score
            AnimatedContent(
              targetState = d.currentRunsAndWickets,
              modifier = Modifier.sharedContent(teamScoreKey(matchId, showTeam1)),
              transitionSpec = { dataSwap(dataSlide, dataFade) },
              label = "score",
            ) { score ->
              Text(
                text = score,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overs
            Text(
              text = d.currentFormattedOvers,
              style = MaterialTheme.typography.displaySmall,
              fontWeight = FontWeight.Normal,
              textAlign = TextAlign.Center,
            )
          }
        }
      }

      // Toggle Buttons at the bottom
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
      ) {
        ToggleButton(
          checked = showTeam1,
          onCheckedChange = { showTeam1 = true },
          modifier = Modifier.weight(1f),
          shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
        ) {
          if (showTeam1) {
            Icon(
              painter = painterResource(Res.drawable.ic_check),
              contentDescription = null,
              modifier = Modifier.size(ToggleButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          }
          Text(text = stringResource(Res.string.team_1))
        }
        ToggleButton(
          checked = !showTeam1,
          onCheckedChange = { showTeam1 = false },
          modifier = Modifier.weight(1f),
          shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
        ) {
          if (!showTeam1) {
            Icon(
              painter = painterResource(Res.drawable.ic_check),
              contentDescription = null,
              modifier = Modifier.size(ToggleButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          }
          Text(text = stringResource(Res.string.team_2))
        }
      }
    }
  }
}

@DarkRealDevicePreview
@Composable
private fun MatchCompleteUIPreview() {
  val sampleMatchRecord =
    MatchRecord(
      matchRecordId = "sample_id",
      startDateTime = 0L,
      endDateTime = 0L,
      team1Detail = TeamDetail(teamName = "Team 1", runs = 150, wickets = 5, balls = 120),
      team2Detail = TeamDetail(teamName = "Team 2", runs = 145, wickets = 8, balls = 120),
      ballsPerInning = 120,
      didTeam1WonToss = true,
      isTeam1BattingFirst = true,
      isFirstInningComplete = true,
      rrrAtSecondInningStart = "7.5",
      status = MatchStatus.TEAM_2_WON,
      matchAdminID = "admin_id",
    )
  val team1Details = sampleMatchRecord.toBasicMatchUIDetails(true)
  val team2Details = sampleMatchRecord.toBasicMatchUIDetails(false)

  AppTheme { MatchCompleteUI(onBack = {}, sampleMatchRecord, team1Details, team2Details) }
}
