package io.github.raghavsatyadev.library.support.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.raghavsatyadev.library.support.components.CREATE_MATCH_KEY
import io.github.raghavsatyadev.library.support.components.LocalEntryVisibilityScope
import io.github.raghavsatyadev.library.support.components.copyMatchKey
import io.github.raghavsatyadev.library.support.models.db.match_record.MatchStatus
import io.github.raghavsatyadev.library.ui.create_match.CreateMatchScreen
import io.github.raghavsatyadev.library.ui.dashboard.DashboardScreen
import io.github.raghavsatyadev.library.ui.match_complete.MatchCompleteScreen
import io.github.raghavsatyadev.library.ui.match_record.MatchRecordScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/** The app's back stack, for screens declared in [navigationModule]; [AppNavHost] provides it. */
val LocalBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> { error("No back stack") }

/** Metadata key: the screen grows out of a card (a container transform) instead of fading. */
internal const val CONTAINER_TRANSFORM = "containerTransform"

private val grows = mapOf<String, Any>(CONTAINER_TRANSFORM to true)

private fun NavBackStack<NavKey>.pop() {
  if (isNotEmpty()) removeAt(size - 1)
}

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

/** Every screen, one Koin `navigation` entry per route; [AppNavHost] shows them. */
@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
  navigation<AppRoutes.Dashboard> {
    val backStack = LocalBackStack.current
    SharedEntry {
      DashboardScreen(
        onAddMatchClick = { backStack.add(AppRoutes.CreateMatch()) },
        onMatchClick = { matchRecord ->
          val route =
            when (matchRecord.status) {
              MatchStatus.NOT_STARTED,
              MatchStatus.IN_PROGRESS ->
                AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId, preview = matchRecord)
              else ->
                AppRoutes.MatchComplete(matchId = matchRecord.matchRecordId, preview = matchRecord)
            }
          backStack.add(route)
        },
        onCopyMatchRecord = { backStack.add(AppRoutes.CreateMatch(matchRecord = it)) },
      )
    }
  }
  navigation<AppRoutes.CreateMatch>(metadata = grows) { key ->
    val backStack = LocalBackStack.current
    SharedEntry {
      CreateMatchScreen(
        matchRecord = key.matchRecord,
        sharedKey = key.matchRecord?.let { r -> copyMatchKey(r.matchRecordId) } ?: CREATE_MATCH_KEY,
        onMatchCreated = { matchRecord ->
          backStack.pop()
          backStack.add(
            AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId, preview = matchRecord)
          )
        },
      )
    }
  }
  navigation<AppRoutes.MatchRecord>(metadata = grows) { key ->
    val backStack = LocalBackStack.current
    SharedEntry {
      MatchRecordScreen(
        matchRecordId = key.matchId,
        preview = key.preview,
        onBack = { backStack.pop() },
        onMatchCompleted = {
          backStack.pop()
          backStack.add(AppRoutes.MatchComplete(matchId = key.matchId))
        },
      )
    }
  }
  navigation<AppRoutes.MatchComplete>(metadata = grows) { key ->
    val backStack = LocalBackStack.current
    SharedEntry {
      MatchCompleteScreen(
        matchId = key.matchId,
        preview = key.preview,
        onBack = { backStack.pop() },
      )
    }
  }
}
