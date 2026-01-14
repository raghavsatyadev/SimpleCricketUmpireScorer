package io.github.raghavsatyadev.scus.support.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.raghavsatyadev.scus.ui.create_match.CreateMatchScreen
import io.github.raghavsatyadev.scus.ui.dashboard.DashboardScreen
import io.github.raghavsatyadev.scus.ui.match_complete.MatchCompleteScreen
import io.github.raghavsatyadev.scus.ui.match_record.MatchRecordScreen
import io.github.raghavsatyadev.scus.ui.user.LoginScreen
import io.github.raghavsatyadev.support.extensions.replaceAll

@Composable
fun AppNavHost(
    isLoggedIn: Boolean,
    onLoginStateChange: () -> Unit,
) {
    val startRoute = if (isLoggedIn) AppRoutes.Dashboard else AppRoutes.Login
    val backStack = rememberNavBackStack(startRoute)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            backStack.replaceAll(AppRoutes.Dashboard)
        } else {
            backStack.replaceAll(AppRoutes.Login)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<AppRoutes.Dashboard> {
                DashboardScreen(
                    onAddMatchClick = { backStack.add(AppRoutes.CreateMatch()) },
                    onMatchClick = { matchRecord ->
                        backStack.add(AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId))
                    },
                    onCopyMatchRecord = { backStack.add(AppRoutes.CreateMatch(matchRecord = it)) },
                )
            }
            entry<AppRoutes.Login> { LoginScreen { onLoginStateChange() } }
            entry<AppRoutes.CreateMatch> {
                CreateMatchScreen(
                    matchRecord = it.matchRecord,
                    onMatchCreated = { matchRecord ->
                        backStack.removeLastOrNull()
                        backStack.add(AppRoutes.MatchRecord(matchId = matchRecord.matchRecordId))
                    },
                )
            }
            entry<AppRoutes.MatchRecord> { key ->
                MatchRecordScreen(
                    matchRecordId = key.matchId,
                    onBack = { backStack.removeLastOrNull() },
                    onMatchCompleted = {
                        backStack.removeLastOrNull()
                        backStack.add(AppRoutes.MatchComplete(matchId = key.matchId))
                    },
                )
            }
            entry<AppRoutes.MatchComplete> { key ->
                MatchCompleteScreen(
                    matchId = key.matchId,
                    onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
