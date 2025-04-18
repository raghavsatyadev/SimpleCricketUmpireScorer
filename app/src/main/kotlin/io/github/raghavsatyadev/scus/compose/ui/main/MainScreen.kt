package io.github.raghavsatyadev.scus.compose.ui.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.raghavsatyadev.scus.compose.support.routes.MainRoutes
import io.github.raghavsatyadev.scus.compose.support.routes.appGraph

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val rootNavController = rememberNavController()
    NavHost(rootNavController, startDestination = MainRoutes.DashboardScreen) {
        appGraph(rootNavController)
    }
}
