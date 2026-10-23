@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import io.github.raghavsatyadev.library.support.theme.LocalAppearance
import io.github.raghavsatyadev.library.support.theme.LocalThemeReveal
import io.github.raghavsatyadev.library.support.theme.ThemeMode
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import io.github.raghavsatyadev.library.support.theme.changeThemeMode
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.ic_brightness_auto
import scus.composeapp.generated.resources.ic_dark_mode
import scus.composeapp.generated.resources.ic_light_mode
import scus.composeapp.generated.resources.theme_dark
import scus.composeapp.generated.resources.theme_light
import scus.composeapp.generated.resources.theme_system

/**
 * Cycles the theme: follow the device, light, dark. The new theme is uncovered by a circle that
 * grows from this button (a fade with motion reduced). Shows nothing outside the app root, such as
 * in previews.
 */
@Composable
fun ThemeModeButton(modifier: Modifier = Modifier) {
  val appearance = LocalAppearance.current ?: return
  val reveal = LocalThemeReveal.current
  val animate = animationsEnabled()
  var center by remember { mutableStateOf(Offset.Zero) }
  val mode = appearance.mode
  val systemDark = isSystemInDarkTheme()
  IconButton(
    onClick = { changeThemeMode(appearance, mode.next(), reveal, center, animate, systemDark) },
    shapes = IconButtonDefaults.shapes(),
    modifier = modifier.onGloballyPositioned { center = it.boundsInRoot().center },
  ) {
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    AnimatedContent(
      targetState = mode,
      transitionSpec = { (fadeIn(effects) + scaleIn(effects, 0.6f)) togetherWith fadeOut(effects) },
      label = "theme icon",
    ) { shown ->
      Icon(
        painter =
          painterResource(
            when (shown) {
              ThemeMode.System -> Res.drawable.ic_brightness_auto
              ThemeMode.Light -> Res.drawable.ic_light_mode
              ThemeMode.Dark -> Res.drawable.ic_dark_mode
            }
          ),
        contentDescription =
          stringResource(
            when (shown) {
              ThemeMode.System -> Res.string.theme_system
              ThemeMode.Light -> Res.string.theme_light
              ThemeMode.Dark -> Res.string.theme_dark
            }
          ),
      )
    }
  }
}
