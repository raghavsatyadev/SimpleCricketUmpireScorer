@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.github.raghavsatyadev.library.support.components.AppToolBar
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.ENTRANCE_DURATION_MILLIS
import io.github.raghavsatyadev.library.support.components.LightRealDevicePreview
import io.github.raghavsatyadev.library.support.components.StaggeredEntrance
import io.github.raghavsatyadev.library.support.components.ThemeModeButton
import io.github.raghavsatyadev.library.support.components.sharedMatchContainer
import io.github.raghavsatyadev.library.support.components.staggerDelayMillis
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.library.support.theme.AppTheme
import kotlinx.coroutines.delay
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
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val listState = rememberLazyListState()
  val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
  Scaffold(
    modifier =
      Modifier.windowInsetsPadding(WindowInsets.systemBars)
        .nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      AppToolBar(
        title = stringResource(Res.string.app_name),
        scrollBehavior = scrollBehavior,
        actions = { ThemeModeButton() },
      )
    },
  ) { innerPadding ->
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      val (listMatchRecord, btnCreateMatch) = createRefs()
      MatchRecordList(
        modifier =
          Modifier.constrainAs(listMatchRecord) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom) // boxAd.top once AdUI is back
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
          },
        matchRecords = matchRecords,
        listState = listState,
        onMatchClick = onMatchClick,
        onCopyMatchRecord = onCopyMatchRecord,
        onDeleteMatchRecord = onDeleteMatchRecord,
      )
      SmallExtendedFloatingActionButton(
        expanded = fabExpanded,
        text = { Text(stringResource(Res.string.add_match)) },
        icon = {
          Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.add_match),
          )
        },
        onClick = onAddMatchClick,
        modifier =
          Modifier.constrainAs(btnCreateMatch) {
              end.linkTo(parent.end)
              bottom.linkTo(parent.bottom) // boxAd.top once AdUI is back
            }
            .padding(16.dp),
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
  listState: LazyListState,
  onMatchClick: (MatchRecord) -> Unit,
  onCopyMatchRecord: (MatchRecord) -> Unit,
  onDeleteMatchRecord: (MatchRecord) -> Unit,
) {
  val properties = MatchRecordProperties.rememberMatchRecordProperties()
  val motion = MaterialTheme.motionScheme
  // Only the cards composed on the first open play the entrance; cards scrolled in later do not.
  var entranceDone by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(matchRecords.isNotEmpty()) {
    if (matchRecords.isNotEmpty() && !entranceDone) {
      delay((ENTRANCE_DURATION_MILLIS + staggerDelayMillis(matchRecords.size)).toLong())
      entranceDone = true
    }
  }
  LazyColumn(
    state = listState,
    userScrollEnabled = true,
    modifier = modifier,
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 112.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    itemsIndexed(matchRecords, key = { _, record -> record.matchRecordId }) { index, record ->
      StaggeredEntrance(
        index = index,
        play = !entranceDone,
        modifier =
          Modifier.animateItem(
            // The first cards already fade in through the entrance.
            fadeInSpec = if (entranceDone) motion.defaultEffectsSpec() else null,
            placementSpec = motion.defaultSpatialSpec(),
            fadeOutSpec = motion.defaultEffectsSpec(),
          ),
      ) {
        MatchRecordItem(
          modifier =
            Modifier.sharedMatchContainer(record.matchRecordId).fillMaxWidth().wrapContentHeight(),
          matchRecord = record,
          properties = properties,
          onCopyClick = { onCopyMatchRecord(record) },
          onDeleteClick = { onDeleteMatchRecord(record) },
          onMatchClick = onMatchClick,
        )
      }
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
