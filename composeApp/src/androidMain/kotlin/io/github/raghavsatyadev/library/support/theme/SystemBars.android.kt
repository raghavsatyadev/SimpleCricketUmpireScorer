package io.github.raghavsatyadev.library.support.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Draws the system bars edge to edge with icons that read on the app's chosen theme, not the
 * device's. The navigation bar keeps the scrims that `enableEdgeToEdge` uses by default.
 */
@Composable
internal actual fun SystemBarsFollow(dark: Boolean) {
  val activity = LocalActivity.current as? ComponentActivity
  DisposableEffect(activity, dark) {
    activity?.enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
      navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
    )
    onDispose {}
  }
}

private val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
