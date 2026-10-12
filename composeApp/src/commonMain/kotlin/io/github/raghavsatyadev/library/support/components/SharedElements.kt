package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.theme.AppMotion
import io.github.raghavsatyadev.library.support.theme.animationsEnabled

/** The [SharedTransitionScope] around the nav host, or null outside it (previews). */
// The nav host provides both scopes; the cards and screens that share bounds sit several layers
// down, and a parameter would have to pass through every screen in between.
@Suppress("CompositionLocalAllowlist")
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The visibility scope of the nav entry this content is in, or null outside the nav host. */
@Suppress("CompositionLocalAllowlist")
val LocalEntryVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** The corner radius of a match card, which the transform starts from and ends on. */
val MatchCardCorner: Dp = 24.dp

/**
 * The container transform between a match card and the match screen it opens: both mark their
 * container with the same [matchId], and the screen grows out of the card.
 *
 * Each side keeps its own layout and is scaled to fill the moving bounds' width, so text never
 * reflows mid-flight (remeasuring a screen to every in-between size is what makes a transform
 * judder). The bounds move on [AppMotion.screenTransformSpec], which does not bounce: a screen edge
 * that overshoots reads as a glitch. A [fullScreen] side rounds its corners from the card's radius
 * to square as it opens, so nothing snaps at the end.
 *
 * Does nothing outside the nav host or with motion reduced, where the screens just fade.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.sharedMatchContainer(matchId: String, fullScreen: Boolean = false): Modifier {
  val transition = LocalSharedTransitionScope.current ?: return this
  val visibility = LocalEntryVisibilityScope.current ?: return this
  if (!animationsEnabled()) return this
  val spec = AppMotion.screenTransformSpec<Rect>()
  val cornerSpec = AppMotion.screenTransformSpec<Dp>()
  // The side that leaves fades out fast and the side that arrives fades in on the default effects
  // spring, so the card and the screen are never both clearly visible at once.
  val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
  val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
  val corner by
    visibility.transition.animateDp(transitionSpec = { cornerSpec }, label = "match corner") {
      if (fullScreen && it == EnterExitState.Visible) 0.dp else MatchCardCorner
    }
  return with(transition) {
    this@sharedMatchContainer.sharedBounds(
      sharedContentState = rememberSharedContentState(key = "match-$matchId"),
      animatedVisibilityScope = visibility,
      enter = fadeIn(fadeInSpec),
      exit = fadeOut(fadeOutSpec),
      boundsTransform = BoundsTransform { _, _ -> spec },
      resizeMode =
        SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.FillWidth, Alignment.TopCenter),
      clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(corner)),
    )
  }
}
