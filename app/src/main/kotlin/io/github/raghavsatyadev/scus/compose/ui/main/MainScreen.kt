package io.github.raghavsatyadev.scus.compose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.raghavsatyadev.scus.compose.support.routes.AppRoutes
import io.github.raghavsatyadev.scus.compose.support.routes.appGraph

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {

  val rootNavController = rememberNavController()
  val isLoading by viewModel.uiStateManager.isLoading.collectAsStateWithLifecycle()

  Box(modifier = Modifier.fillMaxSize()) {
    NavHost(rootNavController, startDestination = AppRoutes.DashboardScreen) {
      appGraph(rootNavController)
    }

    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    }
  }
}
