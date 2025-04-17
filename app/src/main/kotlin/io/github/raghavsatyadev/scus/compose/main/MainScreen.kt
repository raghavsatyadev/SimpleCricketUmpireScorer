package io.github.raghavsatyadev.scus.compose.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.raghavsatyadev.scus.compose.routes.MainRoutes

@Composable
fun MainScreen(viewModel: MainScreenViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = MainRoutes.DashboardScreen) {
        composable(route = MainRoutes.DashboardScreen::class) { backStackEntry -> }
    }
}
