package io.github.raghavsatyadev.library.support.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlin.math.hypot
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Light, dark, or the device's choice. */
enum class ThemeMode {
  System,
  Light,
  Dark;

  /** The next mode for a button that cycles through them. */
  fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]

  /** Whether this mode shows the dark theme, given the device's own setting. */
  fun isDark(systemDark: Boolean): Boolean =
    when (this) {
      System -> systemDark
      Light -> false
      Dark -> true
    }
}

/** The chosen [ThemeMode] and how to change it. Get it from [LocalAppearance] inside [AppRoot]. */
@Stable
class Appearance internal constructor(mode: ThemeMode) {
  var mode: ThemeMode by mutableStateOf(mode)
    internal set
}

/** The app's [Appearance], or null outside [AppRoot] (previews). */
@Suppress("CompositionLocalAllowlist")
val LocalAppearance = staticCompositionLocalOf<Appearance?> { null }

/** The [ThemeReveal] of the enclosing [AppRoot], or null outside one. */
// The host sits at the root, like the theme, and only a theme button deep in a screen reads it.
@Suppress("CompositionLocalAllowlist")
val LocalThemeReveal = staticCompositionLocalOf<ThemeReveal?> { null }

/**
 * The app's root: [AppTheme] in the chosen [ThemeMode], with a [ThemeReveal] host around the
 * content. Platform entry points use this instead of [AppTheme]. The mode survives configuration
 * changes; it is not stored across app restarts.
 */
@Composable
fun AppRoot(content: @Composable () -> Unit) {
  val appearance =
    rememberSaveable(saver = Saver({ it.mode.name }, { Appearance(ThemeMode.valueOf(it)) })) {
      Appearance(ThemeMode.System)
    }
  val dark = appearance.mode.isDark(isSystemInDarkTheme())
  SystemBarsFollow(dark)
  AppTheme(darkTheme = dark) {
    ThemeRevealHost(themeKey = dark) {
      CompositionLocalProvider(LocalAppearance provides appearance, content = content)
    }
  }
}

/**
 * Changes [appearance] to [next] through the [LocalThemeReveal] from [origin] (in root
 * coordinates), or at once where there is none. A change that keeps the same colours (dark to
 * "device" on a dark device) also applies at once: there is nothing to reveal, and a still would
 * only hide the new icon until the reveal gave up waiting.
 */
fun changeThemeMode(
  appearance: Appearance,
  next: ThemeMode,
  reveal: ThemeReveal?,
  origin: Offset,
  animate: Boolean,
  systemDark: Boolean,
) {
  val same = appearance.mode.isDark(systemDark) == next.isDark(systemDark)
  if (reveal == null || same) appearance.mode = next
  else reveal.start(origin, animate) { appearance.mode = next }
}

/** How the old frame gives way to the new theme. */
enum class RevealStyle {
  /** A circle of the new theme grows from where the user tapped. */
  Circle,

  /** The old frame fades out, for reduced motion. */
  Fade,
}

/**
 * Changes the theme behind a still of the old frame, then uncovers the new theme from where the
 * user tapped. Every frame is recorded into [layer], so a still is ready the moment a change
 * starts.
 */
@Stable
class ThemeReveal
internal constructor(
  private val scope: CoroutineScope,
  private val layer: GraphicsLayer,
  private val themeKey: () -> Any,
) {
  private var still by mutableStateOf<ImageBitmap?>(null)
  private var origin by mutableStateOf(Offset.Zero)
  private val progress = Animatable(0f)
  private var job: Job? = null

  var style: RevealStyle by mutableStateOf(RevealStyle.Circle)
    private set

  /**
   * Takes a still of the screen, runs [change], and once the new theme is on screen uncovers it
   * from [center], or fades the still when [animate] is false. A change that never reaches the
   * screen drops the still after a short wait.
   */
  fun start(center: Offset, animate: Boolean, change: () -> Unit) {
    job?.cancel()
    job = scope.launch {
      val before = themeKey()
      val image = runCatching { layer.toImageBitmap() }.getOrNull()
      if (image == null) {
        change()
        return@launch
      }
      origin = center
      style = if (animate) RevealStyle.Circle else RevealStyle.Fade
      progress.snapTo(0f)
      still = image
      try {
        change()
        val shown =
          withTimeoutOrNull(THEME_WAIT_MILLIS) {
            snapshotFlow { themeKey() }.first { it != before }
          }
        if (shown != null) {
          progress.animateTo(
            1f,
            if (animate) tween(REVEAL_DURATION_MILLIS, easing = RevealEasing)
            else tween(FADE_MILLIS),
          )
        }
      } finally {
        still = null
      }
    }
  }

  internal fun DrawScope.drawStill() {
    val image = still ?: return
    when (style) {
      RevealStyle.Fade -> drawImage(image, alpha = 1f - progress.value)
      RevealStyle.Circle -> {
        // The circle reaches the farthest corner when the reveal ends.
        val farthest =
          max(
            max(hypot(origin.x, origin.y), hypot(size.width - origin.x, origin.y)),
            max(
              hypot(origin.x, size.height - origin.y),
              hypot(size.width - origin.x, size.height - origin.y),
            ),
          )
        val hole = Path().apply { addOval(Rect(origin, farthest * progress.value)) }
        clipPath(hole, ClipOp.Difference) { drawImage(image) }
      }
    }
  }

  private companion object {
    const val THEME_WAIT_MILLIS = 1_000L
    const val FADE_MILLIS = 200

    /**
     * A circle the eye should follow: a timed curve with a soft start. A spring would put most of
     * the growth in the first frames, and read as a cut.
     */
    const val REVEAL_DURATION_MILLIS = 520
    val RevealEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
  }
}

/** Draws [content] and, during a theme change, the still of the old frame on top. */
@Composable
private fun ThemeRevealHost(themeKey: Any, content: @Composable () -> Unit) {
  val scope = rememberCoroutineScope()
  val layer = rememberGraphicsLayer()
  val key by rememberUpdatedState(themeKey)
  val reveal = remember(scope, layer) { ThemeReveal(scope, layer) { key } }
  CompositionLocalProvider(LocalThemeReveal provides reveal) {
    Box(
      modifier =
        Modifier.drawWithContent {
          layer.record { this@drawWithContent.drawContent() }
          drawLayer(layer)
          with(reveal) { drawStill() }
        }
    ) {
      content()
    }
  }
}

/** Light status and navigation bar icons on a [dark] theme, dark icons otherwise (Android). */
@Composable internal expect fun SystemBarsFollow(dark: Boolean)
