@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.LightRealDevicePreview
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.add_match
import scus.composeapp.generated.resources.app_name
import scus.composeapp.generated.resources.ic_add

@Composable
fun DashboardScreen(
  viewModel: DashboardScreenViewModel = koinViewModel(),
  onMatchClick: (MatchRecord) -> Unit,
  onAddMatchClick: () -> Unit,
  onCopyMatchRecord: (MatchRecord) -> Unit,
) {
  val matchRecords by viewModel.matchRecordsFlow.collectAsState(initial = emptyList())

  DashboardUI(
    matchRecords = matchRecords,
    onAddMatchClick = onAddMatchClick,
    onMatchClick = onMatchClick,
    onCopyMatchRecord = onCopyMatchRecord,
    onDeleteMatchRecord = { viewModel.deleteMatchRecord(it) },
  )
}

@Composable
private fun DashboardUI(
  matchRecords: List<MatchRecord>,
  onAddMatchClick: () -> Unit,
  onMatchClick: (MatchRecord) -> Unit,
  onCopyMatchRecord: (MatchRecord) -> Unit,
  onDeleteMatchRecord: (MatchRecord) -> Unit,
) {
  Scaffold(
    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
    topBar = { AppToolBar(title = stringResource(Res.string.app_name)) },
  ) { innerPadding ->
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      val (listMatchRecord, boxAd, btnCreateMatch) = createRefs()
      MatchRecordList(
        modifier =
          Modifier.constrainAs(listMatchRecord) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(boxAd.top)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
          },
        matchRecords = matchRecords,
        onMatchClick = onMatchClick,
        onCopyMatchRecord = onCopyMatchRecord,
        onDeleteMatchRecord = onDeleteMatchRecord,
      )
      MediumFloatingActionButton(
        modifier =
          Modifier.constrainAs(btnCreateMatch) {
              end.linkTo(parent.end)
              bottom.linkTo(boxAd.top)
            }
            .padding(16.dp),
        shape = MaterialTheme.shapes.extraExtraLarge,
        onClick = onAddMatchClick,
        content = {
          Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.add_match),
          )
        },
      )
      // AdUI(
      //   modifier =
      //     Modifier.constrainAs(boxAd) {
      //         start.linkTo(parent.start)
      //         end.linkTo(parent.end)
      //         bottom.linkTo(parent.bottom)
      //         width = Dimension.fillToConstraints
      //         height = Dimension.wrapContent
      //       }
      //       .animateContentSize()
      // )
    }
  }
}

@Composable
private fun MatchRecordList(
  modifier: Modifier,
  matchRecords: List<MatchRecord>,
  onMatchClick: (MatchRecord) -> Unit,
  onCopyMatchRecord: (MatchRecord) -> Unit,
  onDeleteMatchRecord: (MatchRecord) -> Unit,
) {
  val properties = MatchRecordProperties.rememberMatchRecordProperties()
  LazyColumn(
    userScrollEnabled = true,
    modifier = modifier,
    contentPadding = PaddingValues(vertical = 8.dp),
  ) {
    items(matchRecords, key = { it.matchRecordId }) { record ->
      MatchRecordItem(
        modifier =
          Modifier.animateItem()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        matchRecord = record,
        properties = properties,
        onCopyClick = { onCopyMatchRecord(record) },
        onDeleteClick = { onDeleteMatchRecord(record) },
        onMatchClick = onMatchClick,
      )
    }
  }
}

@LightRealDevicePreview
@DarkRealDevicePreview
@Composable
fun DashboardScreenPreview() {
  AppTheme {
    DashboardUI(
      matchRecords = getSampleRecords(),
      onAddMatchClick = {},
      onMatchClick = {},
      onCopyMatchRecord = {},
      onDeleteMatchRecord = {},
    )
  }
}
