@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.support.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.raghavsatyadev.support.theme.AppTheme
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun AppToolBar(
  modifier: Modifier = Modifier,
  title: String,
  actions: @Composable RowScope.() -> Unit = {},
  onNavigateBack: (() -> Unit)? = null,
) {
  CenterAlignedTopAppBar(
    modifier = modifier.fillMaxWidth(),
    title = {
      Text(
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
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    actions = actions,
  )
}

@LightPreview
@DarkPreview
@Composable
fun AppToolBarPreview() {
  AppTheme { AppToolBar(title = "SCUS", onNavigateBack = {}) }
}

@LightPreview
@DarkPreview
@Composable
fun AppToolBarWithoutBackButtonPreview() {
  AppTheme { AppToolBar(title = "SCUS", onNavigateBack = null) }
}
