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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.raghavsatyadev.library.support.components.CREATE_MATCH_KEY
import io.github.raghavsatyadev.library.support.components.LocalEntryVisibilityScope
import io.github.raghavsatyadev.library.support.components.LocalSharedTransitionScope
import io.github.raghavsatyadev.library.support.components.copyMatchKey
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import io.github.raghavsatyadev.library.support.theme.animationsEnabled
import io.github.raghavsatyadev.library.ui.create_match.CreateMatchScreen
import io.github.raghavsatyadev.library.ui.dashboard.DashboardScreen
import io.github.raghavsatyadev.library.ui.match_complete.MatchCompleteScreen
import io.github.raghavsatyadev.library.ui.match_record.MatchRecordScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

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

/**
 * Gives [content] the visibility scope of its nav entry, so a card and the screen it opens can
 * share bounds (see sharedMatchContainer).
 */
@Composable
private fun SharedEntry(content: @Composable () -> Unit) {
  CompositionLocalProvider(
    LocalEntryVisibilityScope provides LocalNavAnimatedContentScope.current,
    content = content,
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
  val containerTransform =
    NavDisplay.transitionSpec { fadeThrough } +
      NavDisplay.popTransitionSpec { fadeThrough } +
      NavDisplay.predictivePopTransitionSpec { fadeThrough }

  SharedTransitionLayout {
    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
      NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
        sharedTransitionScope = this,
        transitionSpec = { quickFade },
        popTransitionSpec = { quickFade },
        // The system back button and gesture too, not the library's default slide and scale.
        predictivePopTransitionSpec = { quickFade },
        entryProvider =
          entryProvider {
            entry<AppRoutes.Dashboard> {
              SharedEntry {
                DashboardScreen(
                  onAddMatchClick = { backStack.add(AppRoutes.CreateMatch()) },
                  onMatchClick = { matchRecord ->
                    val route =
                      when (matchRecord.status) {
                        MatchStatus.NOT_STARTED,
                        MatchStatus.IN_PROGRESS -> {
                          AppRoutes.MatchRecord(
                            matchId = matchRecord.matchRecordId,
                            preview = matchRecord,
                          )
                        }
                        else -> {
                          AppRoutes.MatchComplete(
                            matchId = matchRecord.matchRecordId,
                            preview = matchRecord,
                          )
                        }
                      }
                    backStack.add(route)
                  },
                  onCopyMatchRecord = { backStack.add(AppRoutes.CreateMatch(matchRecord = it)) },
                )
              }
            }
            entry<AppRoutes.CreateMatch>(metadata = containerTransform) {
              SharedEntry {
                CreateMatchScreen(
                  matchRecord = it.matchRecord,
                  sharedKey =
                    it.matchRecord?.let { r -> copyMatchKey(r.matchRecordId) } ?: CREATE_MATCH_KEY,
                  onMatchCreated = { matchRecord ->
                    if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
                    backStack.add(
                      AppRoutes.MatchRecord(
                        matchId = matchRecord.matchRecordId,
                        preview = matchRecord,
                      )
                    )
                  },
                )
              }
            }
            entry<AppRoutes.MatchRecord>(metadata = containerTransform) { key ->
              SharedEntry {
                MatchRecordScreen(
                  matchRecordId = key.matchId,
                  preview = key.preview,
                  onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
                  onMatchCompleted = {
                    if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
                    backStack.add(AppRoutes.MatchComplete(matchId = key.matchId))
                  },
                )
              }
            }
            entry<AppRoutes.MatchComplete>(metadata = containerTransform) { key ->
              SharedEntry {
                MatchCompleteScreen(
                  matchId = key.matchId,
                  preview = key.preview,
                  onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
                )
              }
            }
          },
      )
    }
  }
}

/** How long the list stays behind a growing card: about as long as the transform takes. */
private const val HOLD_MILLIS = 380

private const val HOLD_FADE_MILLIS = 120

private const val QUICK_FADE_IN_MILLIS = 150
private const val QUICK_FADE_OUT_MILLIS = 90
