@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.Alignment
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** How big a celebration is: a wicket is small, a milestone is the bigger moment. */
enum class CelebrationSize(
  val pieces: Int,
  val radius: Dp,
  val pieceSize: Dp,
  val durationMillis: Int,
) {
  Small(pieces = 8, radius = 96.dp, pieceSize = 12.dp, durationMillis = 420),
  Large(pieces = 12, radius = 150.dp, pieceSize = 16.dp, durationMillis = 640),
}

/**
 * A one-shot burst of Material shapes flying out from the centre and fading, for an event worth
 * celebrating (a wicket, a run milestone). It plays each time [trigger] grows; 0 never plays, so
 * opening a screen shows nothing. Decorative only — the score that changed carries the meaning —
 * and nothing is drawn when animations are off. It takes no input, so taps go straight through.
 */
@Composable
fun CelebrationBurst(
  trigger: Int,
  color: Color,
  modifier: Modifier = Modifier,
  size: CelebrationSize = CelebrationSize.Small,
  shape: RoundedPolygon = MaterialShapes.Clover4Leaf,
) {
  if (!animationsEnabled()) return
  val progress = remember { Animatable(1f) }
  LaunchedEffect(trigger) {
    if (trigger == 0) return@LaunchedEffect
    // A new event restarts the burst from the centre; the next tap is never held back by it.
    progress.snapTo(0f)
    progress.animateTo(
      1f,
      tween(durationMillis = size.durationMillis, easing = AppMotion.EmphasizedDecelerate),
    )
  }
  val radius = with(LocalDensity.current) { size.radius.toPx() }
  val pieceShape = shape.toShape()
  Box(modifier = modifier.size(size.radius * 2), contentAlignment = Alignment.Center) {
    repeat(size.pieces) { index ->
      val angle = 2 * PI * index / size.pieces + PI / size.pieces
      Box(
        modifier =
          Modifier.size(size.pieceSize)
            .graphicsLayer {
              val p = progress.value
              // Hidden at rest; flies out from 30 % of the radius and fades over the second half.
              alpha = if (p >= 1f) 0f else ((1f - p) * 2f).coerceAtMost(1f)
              val distance = radius * (0.3f + 0.7f * p)
              translationX = (cos(angle) * distance).toFloat()
              translationY = (sin(angle) * distance).toFloat()
              rotationZ = 90f * p + index * 30f
              val scale = 1f - 0.4f * p
              scaleX = scale
              scaleY = scale
            }
            .clip(pieceShape)
            .background(color)
      )
    }
  }
}
