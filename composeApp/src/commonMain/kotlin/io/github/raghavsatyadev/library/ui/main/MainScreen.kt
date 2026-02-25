package io.github.raghavsatyadev.library.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
  isLoading: Boolean = false,
  isLoggedIn: Boolean = false,
  onLoginStateChange: (Boolean) -> Unit = {},
) {
  Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary)) {
    // TODO migrate navigation: AppNavHost(isLoggedIn = isLoggedIn, onLoginStateChange =
    // onLoginStateChange)

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
