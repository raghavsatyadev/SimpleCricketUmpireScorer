@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.raghavsatyadev.library.support.theme.AppTheme
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
  scrollBehavior: TopAppBarScrollBehavior? = null,
) {
  val colors =
    TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
      navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
      titleContentColor = MaterialTheme.colorScheme.onSurface,
      actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  val navigationIcon: @Composable () -> Unit = {
    if (onNavigateBack != null) {
      IconButton(onClick = onNavigateBack, shapes = IconButtonDefaults.shapes()) {
        Icon(
          painter = painterResource(Res.drawable.ic_arrow_back),
          contentDescription = stringResource(Res.string.back),
        )
      }
    }
  }
  if (scrollBehavior != null) {
    MediumFlexibleTopAppBar(
      modifier = modifier.fillMaxWidth(),
      title = {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      },
      titleHorizontalAlignment = Alignment.CenterHorizontally,
      navigationIcon = navigationIcon,
      actions = actions,
      colors = colors,
      scrollBehavior = scrollBehavior,
    )
  } else {
    CenterAlignedTopAppBar(
      modifier = modifier.fillMaxWidth(),
      title = {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      },
      navigationIcon = navigationIcon,
      actions = actions,
      colors = colors,
    )
  }
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

@LightPreview
@DarkPreview
@Composable
fun AppToolBarCollapsingPreview() {
  AppTheme {
    AppToolBar(
      title = "SCUS",
      onNavigateBack = {},
      scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    )
  }
}
