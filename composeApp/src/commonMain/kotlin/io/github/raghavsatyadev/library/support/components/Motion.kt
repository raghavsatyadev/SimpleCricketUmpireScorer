@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.animationsEnabled

/** How long one item takes to fade and rise in. */
const val ENTRANCE_DURATION_MILLIS: Int = 180
private const val STAGGER_STEP_MILLIS = 40
private const val STAGGER_MAX_DELAY_MILLIS = 120
private val EntranceOffset = 16.dp

/**
 * The delay before item [index] starts to enter. Steps are capped, so the whole cascade ends within
 * about 300 ms however many items there are: past that, the last item would feel late.
 */
fun staggerDelayMillis(index: Int): Int =
  (index * STAGGER_STEP_MILLIS).coerceAtMost(STAGGER_MAX_DELAY_MILLIS)

/**
 * Fades and lifts [content] in when [play] is true, after [staggerDelayMillis] for [index]. Pass
 * [play] = true only for items composed on the first open, so it never runs on scroll. With
 * animations off the content is simply there.
 */
@Composable
fun StaggeredEntrance(
  index: Int,
  play: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val animate = play && animationsEnabled()
  val progress = remember { Animatable(if (animate) 0f else 1f) }
  LaunchedEffect(Unit) {
    if (progress.value < 1f) {
      progress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = ENTRANCE_DURATION_MILLIS,
            delayMillis = staggerDelayMillis(index),
            easing = AppMotion.EmphasizedDecelerate,
          ),
      )
    }
  }
  val offset = with(LocalDensity.current) { EntranceOffset.toPx() }
  Box(
    modifier =
      modifier.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offset
      }
  ) {
    content()
  }
}

/**
 * A decorative Material shape that turns once a minute behind a hero moment, such as a match
 * result. Slow enough to notice only when you look for it; still when animations are off. Always
 * decorative: it carries no meaning of its own.
 */
@Composable
fun DriftingShape(
  color: Color,
  modifier: Modifier = Modifier,
  shape: RoundedPolygon = MaterialShapes.Cookie9Sided,
  size: Dp = 200.dp,
  tiltDegrees: Float = 12f,
) {
  // The phase is read in the layer, not in composition, so the turn redraws without recomposing.
  val phase =
    if (animationsEnabled()) {
      rememberInfiniteTransition(label = "drift")
        .animateFloat(0f, 360f, AppMotion.driftSpec(), label = "drift phase")
    } else null
  Box(
    modifier =
      modifier
        .size(size)
        .graphicsLayer { rotationZ = tiltDegrees + (phase?.value ?: 0f) }
        .clip(shape.toShape())
        .background(color)
  )
}
