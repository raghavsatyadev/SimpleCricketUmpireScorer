package io.github.raghavsatyadev.library.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.raghavsatyadev.library.support.navigation.AppNavHost
import io.github.raghavsatyadev.library.support.navigation.AppRoutes
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
  val motion = MaterialTheme.motionScheme
  val isLoading by viewModel.isLoading.collectAsState()

  Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.surface)) {
    AppNavHost(AppRoutes.Dashboard)

    AnimatedVisibility(
      visible = isLoading,
      enter = fadeIn(motion.fastEffectsSpec()),
      exit = fadeOut(motion.fastEffectsSpec()),
    ) {
      Box(
        modifier =
          Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center,
      ) {
        ContainedLoadingIndicator()
      }
    }
  }
}
