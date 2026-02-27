@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.match_complete

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.models.BasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecordExtensions.toBasicMatchUIDetails
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.crr
import scus.composeapp.generated.resources.draw
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
  viewModel: MatchCompleteScreenViewModel = koinViewModel(),
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
  var showTeam1 by
    remember(matchRecord) {
      mutableStateOf(
        matchRecord?.status == MatchStatus.TEAM_1_WON || matchRecord?.status == MatchStatus.DRAW
      )
    }
  val details: BasicMatchUIDetails? = if (showTeam1) team1Details else team2Details

  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = {
      AppToolBar(title = stringResource(Res.string.match_complete_title), onNavigateBack = onBack)
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Main Content centered
      Column(
        modifier = Modifier.weight(1f).fillMaxWidth(),
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
            val color =
              when {
                isWin -> MaterialTheme.colorScheme.primary
                isLoss -> MaterialTheme.colorScheme.error
                isDraw -> MaterialTheme.colorScheme.secondary
                else -> null
              }

            if (textRes != null && color != null) {
              Text(
                text = stringResource(textRes).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }

          // Team Name
          Text(
            text = d.currentTeamName,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )

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
          Text(
            text = d.currentRunsAndWickets,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )

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
          Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          Text(text = stringResource(Res.string.team_1))
        }
        ToggleButton(
          checked = !showTeam1,
          onCheckedChange = { showTeam1 = false },
          modifier = Modifier.weight(1f),
          shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
        ) {
          Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
          Text(text = stringResource(Res.string.team_2))
        }
      }
    }
  }
}

@io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
@Composable
private fun MatchCompleteUIPreview() {
  val sampleMatchRecord =
    MatchRecord(
      matchRecordId = "sample_id",
      startDateTime = 0L,
      endDateTime = 0L,
      team1Detail =
        _root_ide_package_.io.github.raghavsatyadev.library.support.models.db.match_record
          .TeamDetail(teamName = "Team 1", runs = 150, wickets = 5, balls = 120),
      team2Detail =
        _root_ide_package_.io.github.raghavsatyadev.library.support.models.db.match_record
          .TeamDetail(teamName = "Team 2", runs = 145, wickets = 8, balls = 120),
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

  _root_ide_package_.io.github.raghavsatyadev.library.support.theme.AppTheme {
    MatchCompleteUI(onBack = {}, sampleMatchRecord, team1Details, team2Details)
  }
}
