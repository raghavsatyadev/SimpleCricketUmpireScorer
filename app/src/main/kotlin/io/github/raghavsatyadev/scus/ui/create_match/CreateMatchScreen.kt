@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.scus.ui.create_match

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.components.AppToolBar
import io.github.raghavsatyadev.support.components.DarkPreview
import io.github.raghavsatyadev.support.components.ErrorDialog
import io.github.raghavsatyadev.support.extensions.DateExtensions.formatToDateString
import io.github.raghavsatyadev.support.extensions.DateExtensions.toZoneEpochMillis
import io.github.raghavsatyadev.support.extensions.DateExtensions.toZoneLocalDate
import io.github.raghavsatyadev.support.extensions.DateExtensions.toZoneLocalDateTime
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.essential.UiState
import io.github.raghavsatyadev.support.theme.AppTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

@Composable
fun CreateMatchScreen(
  matchRecord: MatchRecord? = null,
  viewModel: CreateMatchScreenViewModel = koinViewModel(),
  onMatchCreated: (MatchRecord) -> Unit = {},
) {

  LaunchedEffect(matchRecord) {
    if (matchRecord != null) {
      viewModel.setMatchRecord(matchRecord)
    } else {
      viewModel.resetMatchRecord()
    }
  }

  HandleCreateMatchEvents(viewModel, onMatchCreated)

  var showDateTimeDialogs by remember { mutableStateOf(false) }

  // Use LocalDateTime as the main state
  var selectedDateTime by remember {
    mutableStateOf(matchRecord?.startDateTime?.toZoneLocalDateTime() ?: LocalDateTime.now())
  }

  DateTimePickerDialogUI(
    selectedDateTime = selectedDateTime,
    showDatePickerDialog = showDateTimeDialogs,
    hideDatePickerDialog = { showDateTimeDialogs = false },
    onDateTimeSelected = {
      selectedDateTime = it
      showDateTimeDialogs = false
    },
  )

  CreateMatchRecordUI(
    selectedDateTime = selectedDateTime,
    onMatchDateTimeClick = { showDateTimeDialogs = true },
    onSaveMatchRecord = { dateTime, team1, team2, over, toss, bat, location ->
      viewModel.saveMatchRecord(
        dateTime.toZoneEpochMillis(),
        team1,
        team2,
        over,
        toss,
        bat,
        location,
      )
    },
    initialTeam1Name = matchRecord?.team1Detail?.teamName ?: "",
    initialTeam2Name = matchRecord?.team2Detail?.teamName ?: "",
    initialInningOver = matchRecord?.ballsPerInning?.div(6)?.toString() ?: "",
    initialMatchLocation = matchRecord?.location ?: "",
    initialTossWonByTeam1 = matchRecord?.didTeam1WonToss ?: true,
    initialBatFirstByTeam1 = matchRecord?.isTeam1BattingFirst ?: true,
  )
}

@Composable
private fun HandleCreateMatchEvents(
  viewModel: CreateMatchScreenViewModel,
  onMatchCreated: (MatchRecord) -> Unit,
) {
  val createMatchState by viewModel.createMatchRecordEvent.collectAsState()

  if (createMatchState is UiState.Error) {
    val state = createMatchState as UiState.Error
    ErrorDialog(
      errorCode = state.error.errorCode,
      errorMessage = state.error.exception?.message ?: stringResource(state.error.errorCode.warning),
    ) {
      viewModel.createMatchEventConsumed()
    }
  }

  val matchRecord = (createMatchState as? UiState.Success)?.data

  LaunchedEffect(matchRecord) {
    matchRecord?.let {
      onMatchCreated(it)
      viewModel.createMatchEventConsumed()
    }
  }
}

@Composable
private fun DateTimePickerDialogUI(
  selectedDateTime: LocalDateTime,
  showDatePickerDialog: Boolean,
  hideDatePickerDialog: () -> Unit,
  onDateTimeSelected: (LocalDateTime) -> Unit,
) {
  var showDatePickerDialogTemp by remember { mutableStateOf(true) }
  var showTimePickerDialogTemp by remember { mutableStateOf(false) }

  val datePickerState =
    rememberDatePickerState(initialSelectedDateMillis = selectedDateTime.toZoneEpochMillis())

  val timePickerState =
    rememberTimePickerState(
      initialHour = selectedDateTime.hour,
      initialMinute = selectedDateTime.minute,
      is24Hour = false,
    )

  var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

  if (showDatePickerDialog && showDatePickerDialogTemp) {
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
      onDismissRequest = {
        hideDatePickerDialog()
        showTimePickerDialogTemp = false
        showDatePickerDialogTemp = true
      },
      confirmButton = {
        TextButton(
          onClick = {
            pickedDateMillis = datePickerState.selectedDateMillis
            showDatePickerDialogTemp = false
            showTimePickerDialogTemp = true
          },
          enabled = confirmEnabled,
        ) {
          Text(stringResource(R.string.save))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            hideDatePickerDialog()
            showTimePickerDialogTemp = false
            showDatePickerDialogTemp = true
          }
        ) {
          Text(stringResource(R.string.cancel))
        }
      },
    ) {
      DatePicker(state = datePickerState, modifier = Modifier.verticalScroll(rememberScrollState()))
    }
  }
  if (showTimePickerDialogTemp && pickedDateMillis != null) {
    TimePickerDialog(
      title = { Text(stringResource(R.string.select_time)) },
      onDismissRequest = {
        hideDatePickerDialog()
        showTimePickerDialogTemp = false
        showDatePickerDialogTemp = true
      },
      confirmButton = {
        TextButton(
          onClick = {
            val date = pickedDateMillis?.toZoneLocalDate()
            val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
            if (date != null) {
              onDateTimeSelected(LocalDateTime.of(date, time))
            }
            showTimePickerDialogTemp = false
            showDatePickerDialogTemp = true
          }
        ) {
          Text(stringResource(R.string.save))
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            hideDatePickerDialog()
            showTimePickerDialogTemp = false
            showDatePickerDialogTemp = true
          }
        ) {
          Text(stringResource(R.string.cancel))
        }
      },
    ) {
      TimePicker(state = timePickerState)
    }
  }
}

@Composable
fun ClickableReadOnlyOutlinedTextField(
  value: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  labelContent: @Composable () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  LaunchedEffect(isPressed) { if (isPressed) onClick() }

  OutlinedTextField(
    value = value,
    onValueChange = {},
    readOnly = true,
    interactionSource = interactionSource,
    label = labelContent,
    modifier = modifier.clickable(interactionSource = interactionSource, indication = null) {},
  )
}

@Composable
private fun CreateMatchRecordUI(
  selectedDateTime: LocalDateTime,
  onMatchDateTimeClick: () -> Unit,
  onSaveMatchRecord:
    (
      matchDateTime: LocalDateTime,
      team1Name: String,
      team2Name: String,
      inningOver: String,
      didTeam1WinToss: Boolean,
      isTeam1BattingFirst: Boolean,
      matchLocation: String,
    ) -> Unit,
  initialTeam1Name: String = "",
  initialTeam2Name: String = "",
  initialInningOver: String = "",
  initialMatchLocation: String = "",
  initialTossWonByTeam1: Boolean = true,
  initialBatFirstByTeam1: Boolean = true,
) {
  val matchDateTime = remember(selectedDateTime) { selectedDateTime.formatToDateString() }
  val scrollState = rememberScrollState()
  var team1Name by remember { mutableStateOf(initialTeam1Name) }
  var team2Name by remember { mutableStateOf(initialTeam2Name) }
  var inningOver by remember { mutableStateOf(initialInningOver) }
  var matchLocation by remember { mutableStateOf(initialMatchLocation) }
  val team1Label = stringResource(id = R.string.team_1)
  val team2Label = stringResource(id = R.string.team_2)
  val options = listOf(team1Label, team2Label)
  var tossWonByTeam1 by remember { mutableStateOf(initialTossWonByTeam1) }
  var batFirstByTeam1 by remember { mutableStateOf(initialBatFirstByTeam1) }

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

      ClickableReadOnlyOutlinedTextField(
        value = matchDateTime,
        onClick = onMatchDateTimeClick,
        modifier =
          Modifier.constrainAs(dateTimeRef) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
        labelContent = { Text(text = stringResource(id = R.string.match_date_time)) },
      )

      OutlinedTextField(
        value = team1Name,
        onValueChange = { team1Name = it },
        singleLine = true,
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
        singleLine = true,
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
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = inningOver,
        onValueChange = { newText -> inningOver = newText.filter { it.isDigit() } },
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
        tossWonByTeam1,
        Modifier.constrainAs(tossToggleRef) {
          top.linkTo(tossLabelRef.bottom)
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          width = Dimension.fillToConstraints
        },
        onSelection = { tossWonByTeam1 = it },
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
        isFirstOptionSelected = batFirstByTeam1,
        modifier =
          Modifier.constrainAs(batToggleRef) {
            top.linkTo(batLabelRef.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
          },
        onSelection = { batFirstByTeam1 = it },
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
        onClick = {
          onSaveMatchRecord(
            selectedDateTime,
            team1Name,
            team2Name,
            inningOver,
            tossWonByTeam1,
            batFirstByTeam1,
            matchLocation,
          )
        },
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
  isFirstOptionSelected: Boolean,
  modifier: Modifier,
  onSelection: (Boolean) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    options.forEachIndexed { index, label ->
      val isSelected = if (index == 0) isFirstOptionSelected else !isFirstOptionSelected
      ToggleButton(
        checked = isSelected,
        onCheckedChange = { if (index == 0) onSelection(true) else onSelection(false) },
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

@DarkPreview
@Composable
fun CreateMatchRecordScreenPreview() {
  AppTheme {
    CreateMatchRecordUI(
      selectedDateTime = Calendar.getInstance().timeInMillis.toZoneLocalDateTime(),
      onMatchDateTimeClick = {},
      onSaveMatchRecord = { _, _, _, _, _, _, _ -> },
    )
  }
}
