package io.github.raghavsatyadev.library.support.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.raghavsatyadev.library.support.components.LocalSharedTransitionScope
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

private val config = SavedStateConfiguration {
  serializersModule = SerializersModule {
    polymorphic(NavKey::class) {
      subclass(AppRoutes.Dashboard::class, AppRoutes.Dashboard.serializer())
      subclass(AppRoutes.CreateMatch::class, AppRoutes.CreateMatch.serializer())
      subclass(AppRoutes.MatchRecord::class, AppRoutes.MatchRecord.serializer())
      subclass(AppRoutes.MatchComplete::class, AppRoutes.MatchComplete.serializer())
    }
  }
}

/** Any screen change that is not a container transform: a quick fade, back and forward alike. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun quickFade(motion: MotionScheme, animate: Boolean): ContentTransform =
  if (animate)
    fadeIn(tween(QUICK_FADE_IN_MILLIS)) togetherWith fadeOut(tween(QUICK_FADE_OUT_MILLIS))
  else fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())

@OptIn(ExperimentalMaterial3ExpressiveApi::class, KoinExperimentalAPI::class)
@Composable
fun AppNavHost(vararg elements: AppRoutes) {

  val backStack = rememberNavBackStack(config, *elements)

  val motion = MaterialTheme.motionScheme
  // A match screen grows out of its card (a container transform). The list stays on screen behind
  // the growing card until the transform ends: fading it out at the same time leaves the card
  // growing over a blank surface of the same colour, and the transform reads as a cut. With motion
  // reduced there is no transform, so the screens fade.
  val fadeThrough =
    if (animationsEnabled()) {
      // The entering screen draws inside the shared bounds; this fade is only there to keep its
      // transition running as long as the transform (with EnterTransition.None the bounds never
      // animate and snap at the end).
      fadeIn(tween(HOLD_MILLIS + HOLD_FADE_MILLIS), initialAlpha = 0.99f) togetherWith
        fadeOut(tween(durationMillis = HOLD_FADE_MILLIS, delayMillis = HOLD_MILLIS))
    } else {
      fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
    }
  val quickFade = quickFade(motion, animationsEnabled())
  // Screens marked CONTAINER_TRANSFORM grow out of a card on the way in and shrink back on the way
  // out.
  fun Scene<NavKey>.grows() = metadata[CONTAINER_TRANSFORM] == true
  val entries = koinEntryProvider<NavKey>()

  SharedTransitionLayout {
    CompositionLocalProvider(
      LocalSharedTransitionScope provides this,
      LocalBackStack provides backStack,
    ) {
      NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
        sharedTransitionScope = this,
        transitionSpec = { if (targetState.grows()) fadeThrough else quickFade },
        popTransitionSpec = { if (initialState.grows()) fadeThrough else quickFade },
        // The system back button and gesture too, not the library's default slide and scale.
        predictivePopTransitionSpec = { if (initialState.grows()) fadeThrough else quickFade },
        entryProvider = entries,
      )
    }
  }
}

/** How long the list stays behind a growing card: about as long as the transform takes. */
private const val HOLD_MILLIS = 380

private const val HOLD_FADE_MILLIS = 120

private const val QUICK_FADE_IN_MILLIS = 150
private const val QUICK_FADE_OUT_MILLIS = 90
