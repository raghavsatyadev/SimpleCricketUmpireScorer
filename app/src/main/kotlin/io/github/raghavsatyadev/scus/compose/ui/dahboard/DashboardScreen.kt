@file:OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalMaterial3ExpressiveApi::class
)

package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.AdUI
import io.github.raghavsatyadev.support.compose.components.AppToolBar
import io.github.raghavsatyadev.support.compose.components.DarkRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun DashboardScreen(
  viewModel: DashboardScreenViewModel = hiltViewModel(),
  onNavigateToLogin: () -> Unit,
) {
  val loginState by remember { mutableStateOf(viewModel.isLoggedIn()) }

  if (!loginState) {
    onNavigateToLogin()
  } else {
    DashboardUI(onAddMatchClick = { /*viewModel.addMatch()*/ })
  }
}

@Composable
private fun DashboardUI(onAddMatchClick: () -> Unit) {
  Scaffold(topBar = { AppToolBar(title = stringResource(Rs.string.app_name)) }) { innerPadding ->
    ConstraintLayout(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val (matchList, adView, addButton) = createRefs()
      MatchRecordList(
        Modifier.constrainAs(matchList) {
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          top.linkTo(parent.top)
          bottom.linkTo(adView.top)
          width = Dimension.fillToConstraints
          height = Dimension.fillToConstraints
        })
      AdUI(
        modifier = Modifier.constrainAs(adView) {
          start.linkTo(parent.start)
          end.linkTo(parent.end)
          bottom.linkTo(parent.bottom)
          width = Dimension.fillToConstraints
          height = Dimension.wrapContent
        })
      MediumFloatingActionButton(
        modifier = Modifier.constrainAs(addButton) {
          end.linkTo(
            parent.end,
            margin = 16.dp
          )
          bottom.linkTo(
            adView.top,
            margin = 16.dp
          )
        },
        shape = MaterialTheme.shapes.extraExtraLarge,
        onClick = onAddMatchClick,
        content = {
          Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = stringResource(R.string.add_match),
          )
        },
      )
    }
  }
}

@Composable
private fun MatchRecordList(modifier: Modifier) {
  LazyColumn(
    userScrollEnabled = true,
    modifier = modifier,
    contentPadding = PaddingValues(vertical = 8.dp),
  ) {
    items(getSampleRecords()) { record ->
      MatchRecordItem(matchRecord = record, onCopyClick = {}, onDeleteClick = {})
    }
  }
}

@LightRealDevicePreview
@DarkRealDevicePreview
@Composable
fun DashboardScreenPreview() {
  DashboardUI(onAddMatchClick = {})
}
