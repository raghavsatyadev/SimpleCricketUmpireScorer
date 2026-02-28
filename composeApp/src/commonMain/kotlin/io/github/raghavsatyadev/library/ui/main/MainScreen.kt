package io.github.raghavsatyadev.library.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.navigation.AppNavHost
import io.github.raghavsatyadev.library.support.navigation.AppRoutes
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
  viewModel: MainViewModel = koinViewModel(),
  onLoginStateChange: (Boolean) -> Unit = {},
) {
  val isLoading by viewModel.isLoading.collectAsState()

  Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary)) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val startRoute = if (isLoggedIn) AppRoutes.Dashboard else AppRoutes.Login

    AppNavHost(
      elements = arrayOf(startRoute),
      isLoggedIn = isLoggedIn,
      onLoginStateChange = { viewModel.changeLoginState() },
    )

    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
      ) {
        ContainedLoadingIndicator(modifier = Modifier.size(80.dp))
      }
    }
  }
}
