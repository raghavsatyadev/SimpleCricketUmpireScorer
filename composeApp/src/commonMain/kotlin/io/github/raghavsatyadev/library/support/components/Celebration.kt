@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * What is being celebrated: a wicket is a quick burst, a run milestone a longer confetti shower.
 */
enum class CelebrationKind(val pieces: Int, val durationMillis: Int, val gravity: Float) {
  Wicket(pieces = 18, durationMillis = 1_300, gravity = 0f),
  Milestone(pieces = 36, durationMillis = 2_200, gravity = 1.1f),
}

private class Piece(
  val angle: Double,
  val speed: Float,
  val spin: Float,
  val size: Float,
  val colorIndex: Int,
  val shapeIndex: Int,
)

/**
 * A celebration over the whole screen: a ring that sweeps out from [origin], Material shapes that
 * burst from it (falling like confetti for a milestone), and [label] popping in above it. It plays
 * each time [trigger] grows; 0 never plays, so opening a screen shows nothing.
 *
 * It never gets in the way of scoring: it has no pointer input, so every tap goes straight through
 * to the buttons under it, and every frame is drawn from layer and draw lambdas, so the screen does
 * not recompose while it runs. A new event restarts it. Decorative only — the score that changed
 * carries the meaning — and nothing is drawn when animations are off.
 */
@Composable
fun CelebrationOverlay(
  trigger: Int,
  kind: CelebrationKind,
  label: String,
  origin: Offset,
  colors: List<Color>,
  modifier: Modifier = Modifier,
) {
  if (!animationsEnabled() || trigger == 0) return
  val progress = remember { Animatable(1f) }
  val labelScale = remember { Animatable(0f) }
  val pieces =
    remember(trigger) {
      val random = Random(trigger * 31 + kind.ordinal)
      List(kind.pieces) {
        Piece(
          angle = 2 * PI * it / kind.pieces + random.nextDouble(-0.25, 0.25),
          speed = random.nextFloat() * 0.45f + 0.55f,
          spin = random.nextFloat() * 540f - 270f,
          size = random.nextFloat() * 0.6f + 0.7f,
          colorIndex = random.nextInt(colors.size.coerceAtLeast(1)),
          shapeIndex = random.nextInt(SHAPES.size),
        )
      }
    }
  LaunchedEffect(trigger) {
    progress.snapTo(0f)
    labelScale.snapTo(0f)
    // The label pops on a bouncy spring (it is decoration, not data) while the burst runs.
    launch {
      labelScale.animateTo(
        1f,
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMediumLow,
        ),
      )
    }
    progress.animateTo(1f, tween(kind.durationMillis, easing = LinearEasing))
  }
  val shapes = SHAPES.map { it.toShape() }
  val ringColor = colors.firstOrNull() ?: MaterialTheme.colorScheme.primary
  val density = LocalDensity.current
  val pieceSize = with(density) { 22.dp.toPx() }

  BoxWithConstraints(modifier = modifier) {
    val reach = with(density) { maxOf(maxWidth, maxHeight).toPx() } * 0.55f
    // The ring: drawn in the draw phase only.
    Canvas(modifier = Modifier.matchParentSizeCompat()) {
      val p = progress.value
      if (p >= 1f) return@Canvas
      val ringP = (p / 0.45f).coerceAtMost(1f)
      drawCircle(
        color = ringColor.copy(alpha = (1f - ringP) * 0.6f),
        radius = reach * 0.9f * AppMotion.EmphasizedDecelerate.transform(ringP),
        center = origin,
        style = Stroke(width = 10.dp.toPx() * (1f - ringP) + 1f),
      )
    }
    pieces.forEach { piece ->
      Box(
        modifier =
          Modifier.offset {
              IntOffset(
                (origin.x - pieceSize / 2).roundToInt(),
                (origin.y - pieceSize / 2).roundToInt(),
              )
            }
            .size(with(density) { (pieceSize * piece.size).toDp() })
            .graphicsLayer {
              val p = progress.value
              if (p >= 1f) {
                alpha = 0f
                return@graphicsLayer
              }
              val t = AppMotion.EmphasizedDecelerate.transform(p)
              val distance = reach * piece.speed * t
              translationX = (cos(piece.angle) * distance).toFloat()
              translationY = (sin(piece.angle) * distance).toFloat() + kind.gravity * reach * p * p
              rotationZ = piece.spin * p
              alpha = if (p < 0.7f) 1f else (1f - p) / 0.3f
              val s = 0.4f + 0.6f * (p * 6f).coerceAtMost(1f)
              scaleX = s
              scaleY = s
            }
            .clip(shapes[piece.shapeIndex])
            .background(colors.getOrElse(piece.colorIndex) { ringColor })
      )
    }
    // The label, above the origin.
    CenteredAt(origin = origin.copy(y = origin.y - with(density) { 72.dp.toPx() })) {
      Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier =
          Modifier.graphicsLayer {
            val p = progress.value
            scaleX = labelScale.value
            scaleY = labelScale.value
            alpha = if (p >= 1f) 0f else if (p < 0.75f) 1f else (1f - p) / 0.25f
          },
      ) {
        Text(
          label,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.ExtraBold,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        )
      }
    }
  }
}

/** Places [content] so its centre sits at [origin]. */
@Composable
private fun CenteredAt(origin: Offset, content: @Composable () -> Unit) {
  Layout(content = content) { measurables, constraints ->
    val placeable = measurables.first().measure(Constraints())
    layout(constraints.maxWidth, constraints.maxHeight) {
      placeable.place(
        (origin.x - placeable.width / 2f).roundToInt(),
        (origin.y - placeable.height / 2f).roundToInt(),
      )
    }
  }
}

private fun Modifier.matchParentSizeCompat(): Modifier =
  this.then(
    Modifier.layout { measurable, constraints ->
      val p = measurable.measure(Constraints.fixed(constraints.maxWidth, constraints.maxHeight))
      layout(p.width, p.height) { p.place(0, 0) }
    }
  )

private val SHAPES =
  listOf(
    MaterialShapes.Clover4Leaf,
    MaterialShapes.Sunny,
    MaterialShapes.Cookie4Sided,
    MaterialShapes.Gem,
    MaterialShapes.Pill,
    MaterialShapes.SoftBurst,
    MaterialShapes.Heart,
    MaterialShapes.Triangle,
  )
