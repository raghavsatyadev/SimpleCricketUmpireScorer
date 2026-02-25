@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.back
import scus.composeapp.generated.resources.ic_arrow_back

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
            painter = painterResource(Res.drawable.ic_arrow_back),
            contentDescription = stringResource(Res.string.back),
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
    _root_ide_package_.io.github.raghavsatyadev.library.support.theme.AppTheme {
        AppToolBar(
            title = "SCUS",
            onNavigateBack = {})
    }
}

@LightPreview
@DarkPreview
@Composable
fun AppToolBarWithoutBackButtonPreview() {
    _root_ide_package_.io.github.raghavsatyadev.library.support.theme.AppTheme {
        AppToolBar(
            title = "SCUS",
            onNavigateBack = null
        )
    }
}
