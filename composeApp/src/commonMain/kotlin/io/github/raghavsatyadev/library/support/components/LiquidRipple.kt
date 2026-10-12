package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A drop of water on the surface: a tap that nothing else handled sends two rings out from the
 * touch point, which refract the content under them, catch a faint [highlight] on their crest, and
 * fade as they grow. It only watches taps and never consumes them; a button's click, a drag or a
 * scroll does not ripple. Pure delight with no meaning of its own, so with [enabled] false
 * (animations off) there is no ripple, and at rest no effect is applied. Platforms without runtime
 * shaders (Android before 13) show no ripple.
 */
fun Modifier.liquidRipple(
  state: LiquidRippleState,
  enabled: Boolean = true,
  highlight: Color = Color.White,
): Modifier {
  if (!enabled) return this
  return this.graphicsLayer {
      renderEffect = if (state.isActive) state.renderEffect(size, this, highlight) else null
    }
    .pointerInput(state) {
      coroutineScope { detectUnhandledTaps { position -> launch { state.play(position) } } }
    }
}

/**
 * Calls [onTap] for each tap that nothing else handled. It reads events in the final pass, after
 * children and scrolling parents had their turn: a child that handles the press consumes the down,
 * and a scroll consumes the moves. A pointer that travels past the touch slop is not a tap.
 */
private suspend fun PointerInputScope.detectUnhandledTaps(onTap: (Offset) -> Unit) {
  awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
    if (down.isConsumed) return@awaitEachGesture
    val slop = viewConfiguration.touchSlop
    while (true) {
      val change =
        awaitPointerEvent(PointerEventPass.Final).changes.firstOrNull { it.id == down.id }
          ?: return@awaitEachGesture
      if (change.changedToUpIgnoreConsumed()) {
        if (!change.isConsumed) onTap(change.position)
        return@awaitEachGesture
      }
      val travelled = (change.position - down.position).getDistance()
      if (change.isConsumed || travelled > slop) return@awaitEachGesture
    }
  }
}

/** Where a [liquidRipple] started and how far it has spread. A new tap restarts it. */
@Stable
class LiquidRippleState {
  private val progress = Animatable(0f)
  private var origin by mutableStateOf(Offset.Zero)
  private val shader by lazy { RippleShader(LIQUID_RIPPLE_SHADER) }

  /** True while a ripple spreads. The effect is only applied, and only costs, while it is. */
  val isActive: Boolean
    get() = progress.isRunning

  /** Plays a ripple from [origin], in the coordinates of the rippling layout. */
  suspend fun play(origin: Offset) {
    this.origin = origin
    progress.snapTo(0f)
    // Linear: a ring on water spreads at a steady speed; the shader's fade gives it its ease.
    progress.animateTo(1f, tween(durationMillis = RIPPLE_DURATION_MILLIS, easing = LinearEasing))
  }

  /** The effect for the current frame. Read it only while drawing. */
  internal fun renderEffect(size: Size, density: Density, highlight: Color): RenderEffect? {
    val wavelength = with(density) { RippleWavelength.toPx() }
    val farthest =
      max(
        max(hypot(origin.x, origin.y), hypot(size.width - origin.x, origin.y)),
        max(
          hypot(origin.x, size.height - origin.y),
          hypot(size.width - origin.x, size.height - origin.y),
        ),
      )
    return shader.effect(
      RippleUniforms(
        width = size.width,
        height = size.height,
        originX = origin.x,
        originY = origin.y,
        progress = progress.value,
        // The first ring reaches the farthest corner just as the ripple ends.
        maxRadius = farthest + wavelength,
        amplitude = with(density) { RippleAmplitude.toPx() },
        wavelength = wavelength,
        highlight = highlight,
        highlightStrength = HIGHLIGHT_STRENGTH,
      )
    )
  }
}

@Composable fun rememberLiquidRippleState(): LiquidRippleState = remember { LiquidRippleState() }

/** The values the ripple shader reads each frame, in pixels. */
internal class RippleUniforms(
  val width: Float,
  val height: Float,
  val originX: Float,
  val originY: Float,
  val progress: Float,
  val maxRadius: Float,
  val amplitude: Float,
  val wavelength: Float,
  val highlight: Color,
  val highlightStrength: Float,
)

/**
 * The platform's runtime shader for [LIQUID_RIPPLE_SHADER]: AGSL on Android 13+, SkSL through Skia
 * elsewhere. [effect] returns null where runtime shaders are not available.
 */
internal expect class RippleShader(source: String) {
  fun effect(uniforms: RippleUniforms): RenderEffect?
}

private const val RIPPLE_DURATION_MILLIS = 800

/** How far a ring pushes the content, at its strongest. A few pixels read as water, not a wave. */
private val RippleAmplitude = 3.dp

/** The distance between two crests, and so the width of each ring. */
private val RippleWavelength = 28.dp

/** How much of the highlight colour the crest of the first ring shows. */
private const val HIGHLIGHT_STRENGTH = 0.28f

/**
 * The ripple, in AGSL/SkSL. For each pixel inside the two rings it samples the content a little
 * along the ring's radius instead of right under the pixel: that displacement refracts the content.
 * The rings start softly at the front, decay behind it, and the whole ripple fades with `(1 -
 * progress)^1.5`. Crests are lit in proportion to the square of the wave. Sample points are clamped
 * to the layer, so edges never pull in transparent pixels.
 */
internal const val LIQUID_RIPPLE_SHADER =
  """
uniform shader content;
uniform float2 size;
uniform float2 origin;
uniform float progress;
uniform float maxRadius;
uniform float amplitude;
uniform float wavelength;
layout(color) uniform half4 highlight;
uniform float highlightStrength;

const float TAU = 6.2831853;
const float RINGS = 2.0;

half4 main(float2 coord) {
    float2 delta = coord - origin;
    float dist = length(delta);
    float behind = progress * maxRadius - dist;
    if (behind <= 0.0 || behind >= RINGS * wavelength) {
        return content.eval(coord);
    }
    float envelope = smoothstep(0.0, 0.25 * wavelength, behind) * exp(-behind / wavelength);
    float fade = pow(1.0 - progress, 1.5);
    float wave = sin(TAU * behind / wavelength) * envelope * fade;
    float2 direction = dist > 0.0 ? delta / dist : float2(0.0);
    float2 samplePoint = clamp(coord - direction * wave * amplitude, float2(0.5), size - 0.5);
    half4 color = content.eval(samplePoint);
    float crest = max(wave, 0.0);
    return mix(color, highlight, half(crest * crest * highlightStrength));
}
"""
