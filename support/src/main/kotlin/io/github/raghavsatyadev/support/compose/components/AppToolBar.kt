@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.support.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun AppToolBar(modifier: Modifier = Modifier, title: String, onNavigateBack: (() -> Unit)? = null) {
  TopAppBar(
    modifier = modifier.fillMaxWidth().statusBarsPadding(),
    title = {
      Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.surface,
      )
    },
    navigationIcon = {
      if (onNavigateBack != null) {
        IconButton(onClick = onNavigateBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Rs.string.back),
            tint = MaterialTheme.colorScheme.surface,
          )
        }
      } else {
        Spacer(modifier = Modifier.width(48.dp))
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    actions = { Spacer(modifier = Modifier.width(48.dp)) },
  )
}

@LightPreview
@DarkPreview
@Composable
fun AppToolBarPreview() {
  Column {
    AppToolBar(title = "SCUS", onNavigateBack = {})
    Spacer(modifier = Modifier.height(10.dp))
    AppToolBar(title = "SCUS", onNavigateBack = null)
  }
}
