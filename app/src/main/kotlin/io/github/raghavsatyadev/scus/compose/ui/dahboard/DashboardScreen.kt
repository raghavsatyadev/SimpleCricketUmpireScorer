@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.ComposeAdView

@Composable
fun DashboardScreen(
  viewModel: DashboardScreenViewModel = hiltViewModel(),
  onNavigateToLogin: () -> Unit,
) {
  val loginState by remember { mutableStateOf(viewModel.isLoggedIn()) }

  if (!loginState) {
    onNavigateToLogin()
  } else {
    DashboardView(onAddMatchClick = { /*viewModel.addMatch()*/ })
  }
}

@Composable
private fun DashboardView(onAddMatchClick: () -> Unit) {
  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      Column(modifier = Modifier) {
        LazyColumn(
          modifier =
            Modifier.weight(1f).padding(horizontal = 5.dp).padding(top = 5.dp, bottom = 80.dp),
          contentPadding = PaddingValues(0.dp),
        ) {
          // items(matchRecords) { record -> MatchRecordItem(record = record) }
        }
        ComposeAdView(modifier = Modifier.fillMaxWidth())
      }
      ExtendedFloatingActionButton(
        onClick = onAddMatchClick,
        text = { Text(stringResource(id = R.string.add_match)) },
        icon = {
          Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = stringResource(R.string.add_match),
            tint = MaterialTheme.colorScheme.surfaceVariant,
          )
        },
        modifier = Modifier.align(Alignment.BottomEnd).padding(15.dp),
        containerColor = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@Preview
@Composable
fun DashboardScreenPreview() {
  DashboardView(onAddMatchClick = {})
}
