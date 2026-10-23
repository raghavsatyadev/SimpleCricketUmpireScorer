@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.MotionDurationScale

/**
 * Motion roles. Values read as data (scores, overs, progress) never overshoot: they use the effects
 * springs, which are critically damped. Containers (cards, panes, buttons) use the expressive
 * spatial springs, which may bounce a little.
 */
object AppMotion {
  /** For values read as data: progress ends, numbers. Never overshoots. */
  @Composable
  @ReadOnlyComposable
  fun <T> dataSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowEffectsSpec()

  /** For a number that changes on every tap, such as the score: quick, and never overshoots. */
  @Composable
  @ReadOnlyComposable
  fun <T> tapDataSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastEffectsSpec()

  /** For containers: cards, panes, buttons. May bounce a little. */
  @Composable
  @ReadOnlyComposable
  fun <T> containerSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultSpatialSpec()

  /**
   * For a container that grows into a whole screen (a card opening into its detail). Critically
   * damped: the expressive spatial springs bounce, and a screen edge that overshoots and comes back
   * reads as a glitch. About 450 ms, slow enough for the eye to follow the card.
   */
  fun <T> screenTransformSpec(): FiniteAnimationSpec<T> =
    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = SCREEN_TRANSFORM_STIFFNESS)

  /** For fades and colour changes. */
  @Composable
  @ReadOnlyComposable
  fun <T> effectsSpec(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()

  /**
   * For motion a gesture drives, such as predictive back scrubbing a transition. Linear, so the
   * motion stays under the finger: a spring would run most of its way in the first part of the
   * gesture.
   */
  fun <T> scrubSpec(): FiniteAnimationSpec<T> =
    tween(durationMillis = SCRUB_DURATION_MILLIS, easing = LinearEasing)

  /**
   * For a slow decorative motion that never stops, such as a shape turning behind a result. A
   * linear phase from 0 to 1 per period, so a full turn per period loops without a jump.
   */
  fun <T> driftSpec(): InfiniteRepeatableSpec<T> =
    infiniteRepeatable(tween(durationMillis = DRIFT_PERIOD_MILLIS, easing = LinearEasing))

  /** Material's emphasized decelerate curve: quick to arrive, gentle to settle. */
  val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

  private const val SCRUB_DURATION_MILLIS = 350
  private const val SCREEN_TRANSFORM_STIFFNESS = 380f
  private const val DRIFT_PERIOD_MILLIS = 60_000
}

/**
 * True when the user chose to reduce motion in the app. Read it through [animationsEnabled], which
 * also covers the device's own setting.
 */
@Suppress("CompositionLocalAllowlist") val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * False when motion should be reduced: the device has animations off (animator duration scale 0) or
 * [LocalReduceMotion] is set. Compose animations end at once by themselves when the device has
 * animations off; this flag is for motion Compose does not time (an infinite loop, the moving wave
 * of a wavy indicator) and for choosing a short fade over spatial motion.
 */
@Composable
fun animationsEnabled(): Boolean {
  val scale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
  return scale > 0f && !LocalReduceMotion.current
}
