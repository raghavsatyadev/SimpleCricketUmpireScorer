package io.github.raghavsatyadev.scus.compose.support.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.raghavsatyadev.scus.compose.ui.dahboard.DashboardScreen
import io.github.raghavsatyadev.scus.compose.ui.user.LoginScreen

fun NavGraphBuilder.appGraph(rootNavController: NavHostController) {
    composable<AppRoutes.DashboardScreen> { backStackEntry ->
        DashboardScreen { rootNavController.navigate(AppRoutes.LoginScreen) }
    }
    composable<AppRoutes.LoginScreen> { backStackEntry ->
        LoginScreen { rootNavController.popBackStack() }
    }
}
