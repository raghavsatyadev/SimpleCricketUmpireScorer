package io.github.raghavsatyadev.library.support.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun sharedAxisX(motion: MotionScheme, forward: Boolean): ContentTransform {
  val dir = if (forward) 1 else -1
  return (slideInHorizontally(motion.defaultSpatialSpec()) { (it * 0.3f).toInt() * dir } +
    fadeIn(motion.defaultEffectsSpec())) togetherWith
    (slideOutHorizontally(motion.defaultSpatialSpec()) { -(it * 0.3f).toInt() * dir } +
      fadeOut(motion.fastEffectsSpec()))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavHost(vararg elements: AppRoutes) {

  val backStack = rememberNavBackStack(config, *elements)

  val motion = MaterialTheme.motionScheme

  NavDisplay(
    backStack = backStack,
    onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
    transitionSpec = { sharedAxisX(motion, forward = true) },
    popTransitionSpec = { sharedAxisX(motion, forward = false) },
    predictivePopTransitionSpec = { sharedAxisX(motion, forward = false) },
    entryProvider =
      entryProvider {
        entry<AppRoutes.Dashboard> {
          DashboardScreen(
            onAddMatchClick = { backStack.add(AppRoutes.CreateMatch()) },
            onMatchClick = { matchRecord ->
              val route =
                when (matchRecord.status) {
                  MatchStatus.NOT_STARTED,
                  MatchStatus.IN_PROGRESS -> {
                    AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId)
                  }
                  else -> {
                    AppRoutes.MatchComplete(matchId = matchRecord.matchRecordId)
                  }
                }
              backStack.add(route)
            },
            onCopyMatchRecord = { backStack.add(AppRoutes.CreateMatch(matchRecord = it)) },
          )
        }
        entry<AppRoutes.CreateMatch> {
          CreateMatchScreen(
            matchRecord = it.matchRecord,
            onMatchCreated = { matchRecord ->
              if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
              backStack.add(AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId))
            },
          )
        }
        entry<AppRoutes.MatchRecord> { key ->
          MatchRecordScreen(
            matchRecordId = key.matchId,
            onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
            onMatchCompleted = {
              if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1)
              backStack.add(AppRoutes.MatchComplete(matchId = key.matchId))
            },
          )
        }
        entry<AppRoutes.MatchComplete> { key ->
          MatchCompleteScreen(
            matchId = key.matchId,
            onBack = { if (backStack.isNotEmpty()) backStack.removeAt(backStack.size - 1) },
          )
        }
      },
  )
}
