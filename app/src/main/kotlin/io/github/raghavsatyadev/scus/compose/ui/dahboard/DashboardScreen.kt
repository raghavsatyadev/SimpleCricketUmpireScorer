@file:OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)

package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.annotation.Keep
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.scus.compose.ui.dahboard.MatchRecordProperties.Companion.CreateMatchRecordProperties
import io.github.raghavsatyadev.support.compose.components.AdUI
import io.github.raghavsatyadev.support.compose.components.AppToolBar
import io.github.raghavsatyadev.support.compose.components.DarkRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun DashboardScreen(
  viewModel: DashboardScreenViewModel = hiltViewModel(),
  onNavigateToLogin: () -> Unit,
  onMatchClick: (MatchRecord) -> Unit,
) {
  val loginState by remember { mutableStateOf(viewModel.isLoggedIn()) }

  if (!loginState) {
    onNavigateToLogin()
  } else {
    DashboardUI(
      onAddMatchClick = { /*viewModel.addMatch()*/ },
      onMatchClick = onMatchClick
    )
  }
}

@Composable
private fun DashboardUI(
  onAddMatchClick: () -> Unit,
  onMatchClick: (MatchRecord) -> Unit,
) {
  Scaffold(
    topBar = { AppToolBar(title = stringResource(Rs.string.app_name)) },
    floatingActionButton = {
      MediumFloatingActionButton(
        shape = MaterialTheme.shapes.extraExtraLarge,
        onClick = onAddMatchClick,
        content = {
          Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = stringResource(R.string.add_match),
          )
        },
      )
    },
  ) { innerPadding ->
    ConstraintLayout(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val (
        listMatchRecord,
        boxAd,
      ) = createRefs()
      MatchRecordList(
        onMatchClick = onMatchClick,
        modifier = Modifier.constrainAs(listMatchRecord) {
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          top.linkTo(parent.top)
          bottom.linkTo(boxAd.top)
          width = Dimension.fillToConstraints
          height = Dimension.fillToConstraints
        },
      )
      AdUI(
        modifier = Modifier.constrainAs(boxAd) {
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          bottom.linkTo(parent.bottom)
          width = Dimension.fillToConstraints
          height = Dimension.wrapContent
        }
      )
    }
  }
}

@Composable
private fun MatchRecordList(
  modifier: Modifier,
  onMatchClick: (MatchRecord) -> Unit,
) {

  val matchRecordsFlow = remember { getSampleRecords() }

  val matchRecords by matchRecordsFlow.collectAsState()
  CreateMatchRecordProperties { properties ->
    LazyColumn(
      userScrollEnabled = true,
      modifier = modifier,
      contentPadding = PaddingValues(vertical = 8.dp),
    ) {
      items(
        matchRecords,
        key = { it.matchRecordId }) { record ->
        MatchRecordItem(
          modifier = Modifier
            .animateItem()
            .padding(
              vertical = 8.dp,
              horizontal = 16.dp
            )
            .fillMaxWidth()
            .wrapContentHeight(),
          matchRecord = record,
          properties = properties,
          onCopyClick = {},
          onDeleteClick = {},
          onMatchClick = onMatchClick,
        )
      }
    }
  }
}

@Keep
data class MatchRecordProperties(
  val won: String,
  val lost: String,
  val draw: String,
  val inProgress: String,
  val lostColor: Color,
  val winColor: Color,
  val drawColor: Color,
  val inProgressColor: Color,
) {
  companion object {
    @Composable
    fun CreateMatchRecordProperties(onCreated: @Composable (MatchRecordProperties) -> Unit) {
      val won: String = stringResource(R.string.won)
      val lost: String = stringResource(R.string.lost)
      val draw: String = stringResource(R.string.draw)
      val inProgress: String = stringResource(R.string.in_progress)
      val lostColor = colorResource(android.R.color.holo_red_dark)
      val winColor = colorResource(android.R.color.holo_green_dark)
      val drawColor = colorResource(android.R.color.holo_blue_dark)
      val inProgressColor = MaterialTheme.colorScheme.inverseSurface
      onCreated(
        MatchRecordProperties(
          won = won,
          lost = lost,
          draw = draw,
          inProgress = inProgress,
          lostColor = lostColor,
          winColor = winColor,
          drawColor = drawColor,
          inProgressColor = inProgressColor,
        )
      )
    }
  }
}

@LightRealDevicePreview
@DarkRealDevicePreview
@Composable
fun DashboardScreenPreview() {
  DashboardUI(
    onAddMatchClick = {},
    onMatchClick = {})
}
