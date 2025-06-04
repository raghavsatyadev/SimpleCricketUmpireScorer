@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.create_match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.AppToolBar
import io.github.raghavsatyadev.support.compose.components.DarkRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord

@Composable
fun CreateMatchRecordScreen(
  matchRecord: MatchRecord? = null,
  viewModel: CreateMatchRecordScreenViewModel = hiltViewModel(),
) {
  LaunchedEffect(matchRecord) {
    if (matchRecord != null) {
      viewModel.setMatchRecord(matchRecord)
    } else {
      viewModel.resetMatchRecord()
    }
  }
  CreateMatchRecordUI()
}

@Composable
private fun CreateMatchRecordUI() {
  val scrollState = rememberScrollState()
  var matchDateTime by remember { mutableStateOf("") }
  var team1Name by remember { mutableStateOf("") }
  var team2Name by remember { mutableStateOf("") }
  var inningOver by remember { mutableStateOf("") }
  var matchLocation by remember { mutableStateOf("") }
    val team1Label = stringResource(id = R.string.team_1)
  val team2Label = stringResource(id = R.string.team_2)
  val options = listOf(team1Label, team2Label)
  var selectedIndexToss by remember { mutableIntStateOf(0) }
  var selectedIndexBat by remember { mutableIntStateOf(0) }

  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = { AppToolBar(title = stringResource(R.string.create_match_title)) },
  ) { innerPadding ->
    ConstraintLayout(
      modifier =
        Modifier.fillMaxSize().padding(innerPadding).verticalScroll(scrollState).padding(15.dp)
    ) {
      val (
        dateTimeRef,
        team1Ref,
        team2Ref,
        overRef,
        tossLabelRef,
        tossToggleRef,
        batLabelRef,
        batToggleRef,
        locationRef,
        saveBtnRef) =
        createRefs()

      OutlinedTextField(
        value = matchDateTime,
        onValueChange = { matchDateTime = it },
        label = { Text(stringResource(id = R.string.match_date_time)) },
        modifier =
          Modifier.constrainAs(dateTimeRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
      )

      OutlinedTextField(
        value = team1Name,
        onValueChange = { team1Name = it },
        label = { Text(stringResource(id = R.string.team_1_name)) },
        modifier =
          Modifier.constrainAs(team1Ref) {
            top.linkTo(dateTimeRef.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
      )

      OutlinedTextField(
        value = team2Name,
        onValueChange = { team2Name = it },
        label = { Text(stringResource(id = R.string.team_2_name)) },
        modifier =
          Modifier.constrainAs(team2Ref) {
            top.linkTo(team1Ref.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
      )

      OutlinedTextField(
        value = inningOver,
        onValueChange = { inningOver = it },
        label = { Text(stringResource(id = R.string.overs_per_inning)) },
        modifier =
          Modifier.constrainAs(overRef) {
            top.linkTo(team2Ref.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
        singleLine = true,
      )

      Text(
        text = stringResource(id = R.string.toss_won_by),
        style = MaterialTheme.typography.titleLarge,
        modifier =
          Modifier.constrainAs(tossLabelRef) {
            top.linkTo(overRef.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
          },
      )

      ToggleButtonGroup(
        options,
        selectedIndexToss,
        Modifier.constrainAs(tossToggleRef) {
          top.linkTo(tossLabelRef.bottom)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          width = Dimension.fillToConstraints
        },
        onSelection = { index, isChecked -> selectedIndexToss = index },
      )

      Text(
        text = stringResource(id = R.string.which_team_bats_first),
        style = MaterialTheme.typography.titleLarge,
        modifier =
          Modifier.constrainAs(batLabelRef) {
            top.linkTo(tossToggleRef.bottom, margin = 20.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
          },
      )

      ToggleButtonGroup(
        options = options,
        selectedIndex = selectedIndexBat,
        modifier =
          Modifier.constrainAs(batToggleRef) {
            top.linkTo(batLabelRef.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
        onSelection = { index, isChecked -> selectedIndexBat = index },
      )

      OutlinedTextField(
        value = matchLocation,
        onValueChange = { matchLocation = it },
        label = { Text(stringResource(id = R.string.match_location)) },
        modifier =
          Modifier.constrainAs(locationRef) {
            top.linkTo(batToggleRef.bottom, margin = 10.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
      )

      Button(
        onClick = { /* Save action */ },
        modifier =
          Modifier.constrainAs(saveBtnRef) {
            top.linkTo(locationRef.bottom, margin = 10.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
          },
      ) {
        Text(stringResource(id = R.string.save))
      }
    }
  }
}

@Composable
private fun ToggleButtonGroup(
  options: List<String>,
  selectedIndex: Int,
  modifier: Modifier,
  onSelection: (Int, Boolean) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    options.forEachIndexed { index, label ->
      ToggleButton(
        checked = selectedIndex == index,
        onCheckedChange = { onSelection(index, it) },
        modifier = Modifier.weight(1f),
        shapes =
          when (index) {
            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
          },
      ) {
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(label)
      }
    }
  }
}

@DarkRealDevicePreview
@LightRealDevicePreview
@Composable
fun CreateMatchRecordScreenPreview() {
  CreateMatchRecordUI()
}
