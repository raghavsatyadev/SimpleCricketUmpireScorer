package io.github.raghavsatyadev.scus.compose.support.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.raghavsatyadev.scus.compose.ui.dahboard.DashboardScreen
import io.github.raghavsatyadev.scus.compose.ui.main.MainViewModel
import io.github.raghavsatyadev.scus.compose.ui.user.LoginScreen
import io.github.raghavsatyadev.support.compose.extesions.NavigationExtensions.replaceAll

@Composable
fun AppNavHost(viewModel: MainViewModel) {
  val backStack = rememberNavBackStack(AppRoutes.Login)
  val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
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
            backStack.add(
              AppRoutes.MatchRecord(
                matchId = matchRecord.matchRecordId,
                matchRecord = matchRecord,
              )
            )
          },
          onCopyMatchRecord = {
            backStack.add(
              AppRoutes.MatchRecord(
                matchId = it.matchRecordId,
                matchRecord = it
              )
            )
          },
        )
      }
      entry<AppRoutes.Login> { LoginScreen { viewModel.changeLoginState() } }

      entry<AppRoutes.CreateMatch> {}
      entry<AppRoutes.MatchRecord> { key -> }
    },
  )
}
