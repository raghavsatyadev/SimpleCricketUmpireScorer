package io.github.raghavsatyadev.library.support.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
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

/** The shared key of the create-match screen opened from the dashboard's add button. */
const val CREATE_MATCH_KEY = "create-match"

/** The corner radius of the add-match button, which the create screen grows from. */
val FAB_CORNER: Dp = 16.dp

/** The shared key of the create-match screen opened by copying match [matchId]. */
fun copyMatchKey(matchId: String): String = "copy-$matchId"

/**
 * The pieces of a match card that fly on their own into the screen it opens, inside the container
 * transform: each team's name, score, overs and result pill. [team1] picks the team.
 */
fun teamNameKey(matchId: String, team1: Boolean): String = "match-$matchId-name-${team(team1)}"

fun teamScoreKey(matchId: String, team1: Boolean): String = "match-$matchId-score-${team(team1)}"

fun teamOversKey(matchId: String, team1: Boolean): String = "match-$matchId-overs-${team(team1)}"

fun teamStatusKey(matchId: String, team1: Boolean): String = "match-$matchId-status-${team(team1)}"

/** A match card's single status pill ("In Progress"), which the scoring screen keeps. */
fun matchStatusKey(matchId: String): String = "match-$matchId-status"

/** Where and when a match was played, as the card shows it. */
fun matchLocationKey(matchId: String): String = "match-$matchId-location"

fun matchTimingsKey(matchId: String): String = "match-$matchId-timings"

private fun team(team1: Boolean) = if (team1) 1 else 2

/** The corner radius of a match card, which the transform starts from and ends on. */
val MatchCardCorner: Dp = 24.dp

/**
 * The container transform between a match card and the match screen it opens: both mark their
 * container with the same [matchId], and the screen grows out of the card. See [sharedContainer].
 */
@Composable
fun Modifier.sharedMatchContainer(matchId: String, fullScreen: Boolean = false): Modifier =
  sharedContainer(key = "match-$matchId", fullScreen = fullScreen)

/**
 * A container transform: the small side (a card, a FAB) and the screen it opens mark their
 * containers with the same [key], and the screen grows out of the small side.
 *
 * Each side keeps its own final layout, clipped by the moving bounds, so text never reflows
 * mid-flight (remeasuring a screen to every in-between size is what makes a transform judder). The
 * bounds move on [AppMotion.screenTransformSpec], which does not bounce: a screen edge that
 * overshoots reads as a glitch. A [fullScreen] side rounds its corners from [corner] to square as
 * it opens, so nothing snaps at the end.
 *
 * Does nothing outside the nav host or with motion reduced, where the screens just fade.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.sharedContainer(
  key: String,
  fullScreen: Boolean = false,
  corner: Dp = MatchCardCorner,
): Modifier {
  val transition = LocalSharedTransitionScope.current ?: return this
  val visibility = LocalEntryVisibilityScope.current ?: return this
  if (!animationsEnabled()) return this
  val spec = AppMotion.screenTransformSpec<Rect>()
  val cornerSpec = AppMotion.screenTransformSpec<Dp>()
  // Closing mirrors opening: the screen stays opaque while it shrinks and fades only at the end,
  // where the small side fades back in. On open the small side fades out fast and the screen fades
  // in, so the two are never both clearly visible at once.
  val enter =
    if (fullScreen) fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec())
    else fadeIn(tween(LATE_FADE_MILLIS, delayMillis = LATE_FADE_DELAY_MILLIS))
  val exit =
    if (fullScreen) fadeOut(tween(LATE_FADE_MILLIS, delayMillis = LATE_FADE_DELAY_MILLIS))
    else fadeOut(MaterialTheme.motionScheme.fastEffectsSpec())
  val animatedCorner by
    visibility.transition.animateDp(transitionSpec = { cornerSpec }, label = "container corner") {
      if (fullScreen && it == EnterExitState.Visible) 0.dp else corner
    }
  return with(transition) {
    this@sharedContainer.sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        animatedVisibilityScope = visibility,
        enter = enter,
        exit = exit,
        boundsTransform = BoundsTransform { _, _ -> spec },
        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(animatedCorner)),
      )
      // Each side keeps its own final layout (no reflow) and the moving bounds clip it. Unlike a
      // scaled container, the pieces inside stay where they are drawn, so they fly true.
      .skipToLookaheadSize()
  }
}

/** How long the closing screen holds before it fades, and how long the fade takes. */
private const val LATE_FADE_DELAY_MILLIS = 200
private const val LATE_FADE_MILLIS = 120

/**
 * A piece of content, such as a score, that moves from where it sits on one screen to where it sits
 * on the next, on top of the container transform around it. Both sides use the same [key]; the two
 * versions crossfade (a card's "167-6 (10.0)" becomes the screen's "167-6") and are scaled, never
 * reflowed, while they move. Does nothing outside the nav host or with motion reduced.
 */
@Composable
fun Modifier.sharedContent(key: String): Modifier {
  val transition = LocalSharedTransitionScope.current ?: return this
  val visibility = LocalEntryVisibilityScope.current ?: return this
  if (!animationsEnabled()) return this
  val spec = AppMotion.screenTransformSpec<Rect>()
  return with(transition) {
    this@sharedContent.sharedBounds(
      sharedContentState = rememberSharedContentState(key = key),
      animatedVisibilityScope = visibility,
      boundsTransform = BoundsTransform { _, _ -> spec },
      resizeMode =
        SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit, Alignment.Center),
    )
  }
}
