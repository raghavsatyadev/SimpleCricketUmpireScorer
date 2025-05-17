package io.github.raghavsatyadev.scus.compose.support.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.raghavsatyadev.scus.compose.ui.dahboard.DashboardScreen
import io.github.raghavsatyadev.scus.compose.ui.user.LoginScreen

@Composable
fun AppNavHost(
    rootNavController: NavHostController,
) {
    NavHost(
        navController = rootNavController,
        startDestination = AppRoutes.DashboardScreen
    ) {
        composable<AppRoutes.DashboardScreen> { backStackEntry ->
            DashboardScreen { rootNavController.navigate(AppRoutes.LoginScreen) }
        }
        composable<AppRoutes.LoginScreen> { backStackEntry ->
            LoginScreen { rootNavController.popBackStack() }
        }
    }
}
