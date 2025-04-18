package io.github.raghavsatyadev.scus.compose.support.routes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.raghavsatyadev.scus.compose.ui.dahboard.DashboardScreen
import io.github.raghavsatyadev.scus.compose.ui.user.LoginScreen

fun NavGraphBuilder.appGraph(rootNavController: NavHostController) {
    composable<MainRoutes.DashboardScreen> { backStackEntry ->
        DashboardScreen { rootNavController.navigate(MainRoutes.LoginScreen) }
    }
    composable<MainRoutes.LoginScreen> { backStackEntry ->
        LoginScreen { rootNavController.popBackStack() }
    }
}
