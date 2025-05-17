@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.AdUI
import io.github.raghavsatyadev.support.compose.components.AppToolBar
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview

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
  Scaffold(
    topBar = {
      AppToolBar(title = stringResource(io.github.raghavsatyadev.support.R.string.app_name))
    }) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      Column(modifier = Modifier) {
        MatchRecordList(Modifier)
        AdUI(modifier = Modifier.fillMaxWidth())
      }
      ExtendedFloatingActionButton(
        shape = MaterialTheme.shapes.extraLarge,
        expanded = false,
        onClick = onAddMatchClick,
        text = { Text(stringResource(id = R.string.add_match)) },
        icon = {
          Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = stringResource(R.string.add_match),
            tint = MaterialTheme.colorScheme.surfaceVariant,
          )
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@Composable
private fun ColumnScope.MatchRecordList(modifier: Modifier) {
  LazyColumn(
    userScrollEnabled = true,
    modifier = modifier
      .fillMaxWidth()
      .weight(1f)
      .padding(horizontal = 4.dp)
      .padding(top = 4.dp),
  ) {
    items(getSampleRecords()) { record ->
      MatchRecordItem(matchRecord = record, onCopyClick = {}, onDeleteClick = {})
    }
  }
}

@LightRealDevicePreview
@Composable
fun DashboardScreenPreview() {
  DashboardUI(onAddMatchClick = {})
}
